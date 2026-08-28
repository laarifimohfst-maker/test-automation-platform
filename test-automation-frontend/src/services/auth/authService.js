import api from '../commun/api';
import {
  effacerSession,
  enregistrerJeton,
  enregistrerUtilisateur,
} from './authStorage';

export const inscrire = (nom, email, motDePasse) =>
  api.post('/auth/register', { nom, email, motDePasse });

export const connecter = async (email, motDePasse) => {
  const connexion = await api.post('/auth/login', { email, motDePasse });
  enregistrerJeton(connexion.data.accessToken);

  try {
    const profil = await api.get('/auth/me');
    enregistrerUtilisateur(profil.data);
    return profil.data;
  } catch (erreur) {
    effacerSession();
    throw erreur;
  }
};

export const deconnecter = () => {
  effacerSession();
};
