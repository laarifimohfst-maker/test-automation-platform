import { useCallback, useEffect, useMemo, useState } from 'react';
import AuthContext from './AuthContext';
import {
  obtenirJeton,
  obtenirUtilisateurEnregistre,
  enregistrerUtilisateur,
} from '../services/authStorage';
import {
  connecter as connecterUtilisateur,
  deconnecter as deconnecterUtilisateur,
} from '../services/authService';

function AuthProvider({ children }) {
  const [utilisateur, setUtilisateur] = useState(() =>
    obtenirJeton() ? obtenirUtilisateurEnregistre() : null
  );

  const connecter = useCallback(async (email, motDePasse) => {
    const profil = await connecterUtilisateur(email, motDePasse);
    setUtilisateur(profil);
    return profil;
  }, []);

  const deconnecter = useCallback(() => {
    deconnecterUtilisateur();
    setUtilisateur(null);
  }, []);

  const actualiserUtilisateur = useCallback((profil) => {
    enregistrerUtilisateur(profil);
    setUtilisateur(profil);
  }, []);

  useEffect(() => {
    const gererExpiration = () => setUtilisateur(null);
    window.addEventListener('auth:expiree', gererExpiration);
    return () => window.removeEventListener('auth:expiree', gererExpiration);
  }, []);

  const valeur = useMemo(
    () => ({ utilisateur, connecter, deconnecter, actualiserUtilisateur }),
    [utilisateur, connecter, deconnecter, actualiserUtilisateur]
  );

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
