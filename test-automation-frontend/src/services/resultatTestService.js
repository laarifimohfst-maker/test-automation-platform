import api from './api';

export const obtenirResultatsParExecution = (executionId) =>
  api.get(`/resultats-tests/execution/${executionId}`);