import api from './api';

export const obtenirConfigurationsParProjet = (projetId) =>
  api.get(`/configurations-tests/projet/${projetId}`);

export const configurerTests = (projetId, configurationTest) =>
  api.post(`/projets/${projetId}/configuration-test`, configurationTest);

export const supprimerConfiguration = (id) =>
  api.delete(`/configurations-tests/${id}`);