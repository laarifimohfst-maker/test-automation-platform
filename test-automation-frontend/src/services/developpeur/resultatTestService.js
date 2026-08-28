import api from '../commun/api';

export const obtenirResultatsParExecution = (executionId) =>
  api.get(`/resultats-tests/execution/${executionId}`);
