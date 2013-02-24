/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */

package com.continuuity.internal.app.runtime.flow;

import com.continuuity.api.ApplicationSpecification;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.FlowletDefinition;
import com.continuuity.app.program.Program;
import com.continuuity.app.program.Type;
import com.continuuity.app.runtime.Arguments;
import com.continuuity.app.runtime.ProgramController;
import com.continuuity.app.runtime.ProgramOptions;
import com.continuuity.app.runtime.ProgramRunner;
import com.continuuity.app.runtime.RunId;
import com.continuuity.internal.app.runtime.AbstractProgramController;
import com.continuuity.internal.app.runtime.BasicArguments;
import com.continuuity.internal.app.runtime.ProgramRunnerFactory;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public final class FlowProgramRunner implements ProgramRunner {

  private static final Logger LOG = LoggerFactory.getLogger(FlowProgramRunner.class);

  private final ProgramRunnerFactory programRunnerFactory;

  @Inject
  public FlowProgramRunner(ProgramRunnerFactory programRunnerFactory) {
    this.programRunnerFactory = programRunnerFactory;
  }

  @Override
  public ProgramController run(Program program, ProgramOptions options) {
    // Extract and verify parameters
    ApplicationSpecification appSpec = program.getSpecification();
    Preconditions.checkNotNull(appSpec, "Missing application specification.");

    Type processorType = program.getProcessorType();
    Preconditions.checkNotNull(processorType, "Missing processor type.");
    Preconditions.checkArgument(processorType == Type.FLOW, "Only FLOW process type is supported.");

    FlowSpecification flowSpec = appSpec.getFlows().get(program.getProgramName());
    Preconditions.checkNotNull(flowSpec, "Missing FlowSpecification for %s", program.getProgramName());

    // Launch flowlet program runners
    RunId runId = RunId.generate();
    final Table<String, Integer, ProgramController> flowlets = createFlowlets(program, runId, flowSpec);
    return new FlowProgramController(flowlets, runId, program, flowSpec);
  }

  /**
   * Starts all flowlets in the flow program.
   * @param program Program to run
   * @param flowSpec The {@link FlowSpecification}.
   * @return A {@link Table} with row as flowlet id, column as instance id, cell as the {@link ProgramController}
   *         for the flowlet.
   */
  private Table<String, Integer, ProgramController> createFlowlets(Program program, RunId runId,
                                                                   FlowSpecification flowSpec) {
    Table<String, Integer, ProgramController> flowlets = HashBasedTable.create();

    try {
      for (Map.Entry<String, FlowletDefinition> entry : flowSpec.getFlowlets().entrySet()) {
        int instanceCount = entry.getValue().getInstances();
        for (int instanceId = 0; instanceId < instanceCount; instanceId++) {
          flowlets.put(entry.getKey(), instanceId,
                       startFlowlet(program, new FlowletOptions(entry.getKey(), instanceId, instanceCount, runId)));
        }
      }
    } catch (Throwable t) {
      try {
        // Need to stop all started flowlets
        Futures.successfulAsList(Iterables.transform(flowlets.values(),
          new Function<ProgramController, ListenableFuture<?>>() {
            @Override
            public ListenableFuture<?> apply(ProgramController controller) {
              return controller.stop();
            }
        })).get();
      } catch (Exception e) {
        LOG.error("Fail to stop all flowlets on failure.");
      }
      throw Throwables.propagate(t);
    }
    return flowlets;
  }

  private ProgramController startFlowlet(Program program, ProgramOptions options) {
    return programRunnerFactory.create(ProgramRunnerFactory.Type.FLOWLET)
                               .run(program, options);
  }

  private final static class FlowletOptions implements ProgramOptions {

    private final String name;
    private final Arguments arguments;
    private final Arguments userArguments;

    private FlowletOptions(String name, int instanceId, int instances, RunId runId) {
      this.name = name;
      this.arguments = new BasicArguments(
        ImmutableMap.of("instanceId", Integer.toString(instanceId),
                        "instances", Integer.toString(instances),
                        "runId", runId.getId()));
      this.userArguments = new BasicArguments();
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Arguments getArguments() {
      return arguments;
    }

    @Override
    public Arguments getUserArguments() {
      return userArguments;
    }
  }

  private final class FlowProgramController extends AbstractProgramController {

    private final Table<String, Integer, ProgramController> flowlets;
    private final Program program;
    private final FlowSpecification flowSpec;
    private final Lock lock = new ReentrantLock();

    FlowProgramController(Table<String, Integer, ProgramController> flowlets, RunId runId,
                          Program program, FlowSpecification flowSpec) {
      super(program.getProgramName(), runId);
      this.flowlets = flowlets;
      this.program = program;
      this.flowSpec = flowSpec;
    }

    @Override
    protected void doSuspend() throws Exception {
      LOG.info("Suspending flow: " + flowSpec.getName());
      lock.lock();
      try {
        Futures.successfulAsList(
          Iterables.transform(flowlets.values(),
                              new Function<ProgramController, ListenableFuture<ProgramController>>() {
                                @Override
                                public ListenableFuture<ProgramController> apply(ProgramController input) {
                                  return input.suspend();
                                }
                              })).get();
      } finally {
        lock.unlock();
      }
      LOG.info("Flow suspended: " + flowSpec.getName());
    }

    @Override
    protected void doResume() throws Exception {
      LOG.info("Resuming flow: " + flowSpec.getName());
      lock.lock();
      try {
        Futures.successfulAsList(
          Iterables.transform(flowlets.values(),
                              new Function<ProgramController, ListenableFuture<ProgramController>>() {
                                @Override
                                public ListenableFuture<ProgramController> apply(ProgramController input) {
                                  return input.resume();
                                }
                              })).get();
      } finally {
        lock.unlock();
      }
      LOG.info("Flow resumed: " + flowSpec.getName());
    }

    @Override
    protected void doStop() throws Exception {
      LOG.info("Stoping flow: " + flowSpec.getName());
      lock.lock();
      try {
        Futures.successfulAsList(
          Iterables.transform(flowlets.values(),
                              new Function<ProgramController, ListenableFuture<ProgramController>>() {
                                @Override
                                public ListenableFuture<ProgramController> apply(ProgramController input) {
                                  return input.stop();
                                }
                              })).get();
      } finally {
        lock.unlock();
      }
      LOG.info("Flow stopped: " + flowSpec.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doCommand(String name, Object value) throws Exception {
      if (!"instances".equals(name) || !(value instanceof Map)) {
        return;
      }
      Map<String, Integer> command = (Map<String, Integer>)value;
      lock.lock();
      try {
        for (Map.Entry<String, Integer> entry : command.entrySet()) {
          String flowletName = entry.getKey();
          changeInstances(entry.getKey(), entry.getValue());
        }
      } catch (Throwable t) {
        LOG.error(String.format("Fail to change instances: %s", command), t);
      } finally {
        lock.unlock();
      }
    }

    private void changeInstances(String flowletName, final int newInstanceCount)
                                                      throws ExecutionException, InterruptedException {
      Map<Integer, ProgramController> liveFlowlets = flowlets.row(flowletName);
      int liveCount = liveFlowlets.size();
      if (liveCount == newInstanceCount) {
        return;
      }

      if (liveCount < newInstanceCount) {
        increaseInstances(flowletName, newInstanceCount, liveFlowlets, liveCount);
        return;
      }
      decreaseInstances(flowletName, newInstanceCount, liveFlowlets, liveCount);
    }

    private void increaseInstances(String flowletName, final int newInstanceCount, Map<Integer, ProgramController>
      liveFlowlets, int liveCount) throws InterruptedException, ExecutionException {
      // Wait for all running flowlets completed changing number of instances.
      Futures.successfulAsList(Iterables.transform(
        liveFlowlets.values(),
        new Function<ProgramController, ListenableFuture<?>>() {
          @Override
          public ListenableFuture<?> apply(ProgramController controller) {
            return controller.command("instances", newInstanceCount);
          }
        })).get();

      // Create more instances
      for (int instanceId = liveCount; instanceId < newInstanceCount; instanceId++) {
        flowlets.put(flowletName, instanceId,
                     startFlowlet(program, new FlowletOptions(flowletName, instanceId, newInstanceCount, getRunId())));
      }
    }


    private void decreaseInstances(String flowletName, final int newInstanceCount, Map<Integer, ProgramController>
      liveFlowlets, int liveCount) throws InterruptedException, ExecutionException {
      // Shrink number of flowlets
      // First stop the extra flowlets
      List<ListenableFuture<?>> futures = Lists.newArrayListWithCapacity(liveCount - newInstanceCount);
      for (int instanceId = liveCount - 1; instanceId >= newInstanceCount; instanceId--) {
        futures.add(flowlets.remove(flowletName, instanceId).stop());
      }
      Futures.successfulAsList(futures).get();

      // Then pause all flowlets
      Futures.successfulAsList(Iterables.transform(
        liveFlowlets.values(),
        new Function<ProgramController, ListenableFuture<?>>() {
          @Override
          public ListenableFuture<?> apply(ProgramController controller) {
            return controller.suspend();
          }
        })).get();

      // Next updates instance count for each flowlets
      Futures.successfulAsList(Iterables.transform(
        liveFlowlets.values(),
        new Function<ProgramController, ListenableFuture<?>>() {
          @Override
          public ListenableFuture<?> apply(ProgramController controller) {
            return controller.command("instances", newInstanceCount);
          }
        })).get();

      // Last resume all instances
      Futures.successfulAsList(Iterables.transform(
        liveFlowlets.values(),
        new Function<ProgramController, ListenableFuture<?>>() {
          @Override
          public ListenableFuture<?> apply(ProgramController controller) {
            return controller.resume();
          }
        })).get();
    }
  }
}
