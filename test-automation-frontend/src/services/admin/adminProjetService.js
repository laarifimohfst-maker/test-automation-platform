import api from '../commun/api';

export const obtenirProjetsAdministration = async (filtres = {}) => {
  const params = Object.fromEntries(
    Object.entries(filtres).filter(([, valeur]) =>
      valeur !== undefined && valeur !== null && valeur !== '' && valeur !== 'TOUS'
    )
  );

  const reponse = await api.get('/projets', { params });
  return reponse.data;
};

export const obtenirProjetAdministration = async (id) => {
  const reponse = await api.get(`/projets/${id}`);
  return reponse.data;
};

export const supprimerProjetAdministration = async (id) => {
  await api.delete(`/projets/${id}`);
};
