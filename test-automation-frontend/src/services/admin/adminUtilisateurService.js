import api from '../commun/api';

export const obtenirUtilisateurs = async () => {
  const reponse = await api.get('/utilisateurs');
  return reponse.data;
};

export const creerUtilisateur = async (utilisateur) => {
  const reponse = await api.post('/utilisateurs', utilisateur);
  return reponse.data;
};

export const modifierUtilisateur = async (id, utilisateur) => {
  const reponse = await api.put(`/utilisateurs/${id}`, utilisateur);
  return reponse.data;
};

export const changerEtatUtilisateur = async (id, actif) => {
  const action = actif ? 'reactiver' : 'desactiver';
  const reponse = await api.patch(`/utilisateurs/${id}/${action}`);
  return reponse.data;
};

export const supprimerUtilisateur = async (id) => {
  await api.delete(`/utilisateurs/${id}`);
};
