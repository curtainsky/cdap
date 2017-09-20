/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import React, {PropTypes} from 'react';
import LoadingSVGCentered from 'components/LoadingSVGCentered';
import Loadable from 'react-loadable';
import CreateStreamStore from 'services/WizardStores/CreateStream/CreateStreamStore';
import UploadDataStore from 'services/WizardStores/UploadData/UploadDataStore';
import PublishPipelineStore from 'services/WizardStores/PublishPipeline/PublishPipelineStore';
import AddNamespaceStore from 'services/WizardStores/AddNamespace/AddNamespaceStore';
import InformationalStore from 'services/WizardStores/Informational/InformationalStore';
import ArtifactUploadStore from 'services/WizardStores/ArtifactUpload/ArtifactUploadStore';
import ApplicationUploadStore from 'services/WizardStores/ApplicationUpload/ApplicationUploadStore';
import OneStepDeployStore from 'services/WizardStores/OneStepDeploy/OneStepDeployStore';
import MicroserviceUploadStore from 'services/WizardStores/MicroserviceUpload/MicroserviceUploadStore';

var StreamCreateWizard = Loadable({
  loader: () => import(/* webpackChunkName: "StreamCreateWizard" */ 'components/CaskWizards/StreamCreate'),
  loading: LoadingSVGCentered
});

var UploadDataWizard = Loadable({
  loader: () => import(/* webpackChunkName: "UploadDataWizard" */ 'components/CaskWizards/UploadData'),
  loading: LoadingSVGCentered
});
var UploadDataUsecaseWizard = Loadable({
  loader: () => import(/* webpackChunkName: "UploadDataUsecaseWizard" */ 'components/CaskWizards/UploadDataUsecase'),
  loading: LoadingSVGCentered
});
var PublishPipelineWizard = Loadable({
  loader: () => import(/* webpackChunkName: "PublishPipelineWizard" */ 'components/CaskWizards/PublishPipeline'),
  loading: LoadingSVGCentered
});
var PublishPipelineUsecaseWizard = Loadable({
  loader: () => import(/* webpackChunkName: "PublishPipelineUsecaseWizard" */ 'components/CaskWizards/PublishPipelineUsecase'),
  loading: LoadingSVGCentered
});
var InformationalWizard = Loadable({
  loader: () => import(/* webpackChunkName: "InformationalWizard" */ 'components/CaskWizards/Informational'),
  loading: LoadingSVGCentered
});
var ArtifactUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "ArtifactUploadWizard" */ 'components/CaskWizards/ArtifactUpload'),
  loading: LoadingSVGCentered
});
var PluginArtifactUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "PluginArtifactUploadWizard" */ 'components/CaskWizards/PluginArtifactUpload'),
  loading: LoadingSVGCentered
});
var ApplicationUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "ApplicationUploadWizard" */ 'components/CaskWizards/ApplicationUpload'),
  loading: LoadingSVGCentered
});
var LibraryUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "LibraryUploadWizard" */ 'components/CaskWizards/LibraryUpload'),
  loading: LoadingSVGCentered
});
var MicroserviceUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "MicroserviceUploadWizard" */ 'components/CaskWizards/MicroserviceUpload'),
  loading: LoadingSVGCentered
});
var MarketArtifactUploadWizard = Loadable({
  loader: () => import(/* webpackChunkName: "MarketArtifactUploadWizard" */ 'components/CaskWizards/MarketArtifactUpload'),
  loading: LoadingSVGCentered
});
var MarketHydratorPluginUpload = Loadable({
  loader: () => import(/* webpackChunkName: "MarketHydratorPluginUploadWizard" */ 'components/CaskWizards/MarketHydratorPluginUpload'),
  loading: LoadingSVGCentered
});
var OneStepDeployApp = Loadable({
  loader: () => import(/* webpackChunkName: "OneStepDeployWizard" */ 'components/CaskWizards/OneStepDeploy/OneStepDeployApp'),
  loading: LoadingSVGCentered
});
var OneStepDeployPlugin = Loadable({
  loader: () => import(/* webpackChunkName: "OneStepDeployWizard" */ 'components/CaskWizards/OneStepDeploy/OneStepDeployPlugin'),
  loading: LoadingSVGCentered
});
var OneStepDeployPluginUsecase = Loadable({
  loader: () => import(/* webpackChunkName: "OneStepDeployWizard" */ 'components/CaskWizards/OneStepDeploy/OneStepDeployPluginUsecase'),
  loading: LoadingSVGCentered
});
var OneStepDeployAppUsecase = Loadable({
  loader: () => import(/* webpackChunkName: "OneStepDeployWizard" */ 'components/CaskWizards/OneStepDeploy/OneStepDeployAppUsecase'),
  loading: LoadingSVGCentered
});
var AddNamespaceWizard = Loadable({
  loader: () => import(/* webpackChunkName: "AddNamespaceWizard" */ 'components/CaskWizards/AddNamespace'),
  loading: LoadingSVGCentered
});

const WizardTypesMap = {
  'create_app': {
    tag: ApplicationUploadWizard,
    store: ApplicationUploadStore
  },
  'create_driver_artifact': {
    tag: MarketArtifactUploadWizard,
    store: ArtifactUploadStore
  },
  'deploy_app': {
    tag: ApplicationUploadWizard,
    store: ApplicationUploadStore
  },
  'create_artifact_rc': {
    tag: ArtifactUploadWizard,
    store: ArtifactUploadStore
  },
  'create_library_rc': {
    tag: LibraryUploadWizard,
    store: ArtifactUploadStore
  },
  'create_plugin_artifact': {
    tag: MarketHydratorPluginUpload,
    store: ArtifactUploadStore
  },
  'create_plugin_artifact_rc': {
    tag: PluginArtifactUploadWizard,
    store: ArtifactUploadStore
  },
  'create_app_rc': {
    tag: ApplicationUploadWizard,
    store: ArtifactUploadStore
  },
  'informational': {
    tag: InformationalWizard,
    store: InformationalStore
  },
  'load_datapack': {
    tag: UploadDataWizard,
    store: UploadDataStore
  },
  'load_datapack_usecase': {
    tag: UploadDataUsecaseWizard,
    store: UploadDataStore
  },
  'create_pipeline': {
    tag: PublishPipelineUsecaseWizard,
    store: PublishPipelineStore
  },
  'create_pipeline_draft': {
    tag: PublishPipelineWizard,
    store: PublishPipelineStore
  },
  'add_namespace': {
    tag: AddNamespaceWizard,
    store: AddNamespaceStore
  },
  'create_stream': {
    tag: StreamCreateWizard,
    store: CreateStreamStore
  },
  'one_step_deploy_app': {
    tag: OneStepDeployApp,
    store: OneStepDeployStore
  },
  'one_step_deploy_app_usecase': {
    tag: OneStepDeployAppUsecase,
    store: OneStepDeployStore
  },
  'one_step_deploy_plugin': {
    tag: OneStepDeployPlugin,
    store: OneStepDeployStore
  },
  'one_step_deploy_plugin_usecase': {
    tag: OneStepDeployPluginUsecase,
    store: OneStepDeployStore
  },
  'create_microservice_rc': {
    tag: MicroserviceUploadWizard,
    store: MicroserviceUploadStore
  }
};

export default function AbstractWizard({isOpen, onClose, wizardType, input, backdrop, displayCTA}) {
  if (!isOpen) {
    return null;
  }
  let {tag: Tag, store} = WizardTypesMap[wizardType];
  if (!Tag) {
    return (<h1> Wizard Type {wizardType} not found </h1>);
  }
  return (
    React.createElement(Tag, {
      isOpen,
      onClose,
      store,
      input,
      backdrop,
      displayCTA
    })
  );
}
AbstractWizard.propTypes = {
  isOpen: PropTypes.bool,
  wizardType: PropTypes.string,
  onClose: PropTypes.func,
  input: PropTypes.any,
  backdrop: PropTypes.bool,
  displayCTA: PropTypes.bool
};
