import api from './api';

export const obtenirUtilisateurParId = (utilisateurId) =>
  api.get(`/utilisateurs/${utilisateurId}`);

export const modifierUtilisateur = (utilisateurId, utilisateur) =>
  api.put(`/utilisateurs/${utilisateurId}`, utilisateur);
