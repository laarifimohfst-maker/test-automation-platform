import api from '../commun/api';

export const obtenirExecutionsAdministration = async (filtres = {}) => {
  const params = Object.fromEntries(
    Object.entries(filtres).filter(([, valeur]) =>
      valeur !== undefined && valeur !== null && valeur !== '' && valeur !== 'TOUS'
    )
  );

  const reponse = await api.get('/executions', { params });
  return reponse.data;
};

export const obtenirExecutionAdministration = async (id) => {
  const reponse = await api.get(`/executions/${id}`);
  return reponse.data;
};

export const supprimerExecutionAdministration = async (id) => {
  await api.delete(`/executions/${id}`);
};
