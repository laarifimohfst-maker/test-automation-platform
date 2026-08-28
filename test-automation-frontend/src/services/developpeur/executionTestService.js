import api from '../commun/api';

export const lancerExecutionTest = (projetId, configurationTestId) =>
  api.post('/executions-tests', null, {
    params: { projetId, configurationTestId },
  });

export const obtenirExecutionsParProjet = (projetId) =>
  api.get(`/executions-tests/projet/${projetId}`);

export const obtenirResultatsParExecution = (executionId) =>
  api.get(`/resultats-tests/execution/${executionId}`);

export const obtenirExecutionParId = (id) =>
  api.get(`/executions-tests/${id}`);

export const supprimerExecutionTest = (executionId) =>
  api.delete(`/executions-tests/${executionId}`);
