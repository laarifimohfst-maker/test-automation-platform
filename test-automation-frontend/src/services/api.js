import axios from 'axios';
import { effacerSession, obtenirJeton } from './authStorage';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

api.interceptors.request.use((configuration) => {
  const jeton = obtenirJeton();

  if (jeton) {
    configuration.headers.Authorization = `Bearer ${jeton}`;
  }

  return configuration;
});

api.interceptors.response.use(
  (reponse) => reponse,
  (erreur) => {
    const estConnexion = erreur.config?.url?.includes('/auth/login');

    if (erreur.response?.status === 401 && !estConnexion) {
      effacerSession();
      window.dispatchEvent(new Event('auth:expiree'));
    }

    return Promise.reject(erreur);
  }
);

export default api;
