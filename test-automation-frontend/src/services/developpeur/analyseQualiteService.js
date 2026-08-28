import api from '../commun/api';

/*
 * Crée une nouvelle exécution d'analyse qualité
 */
export const creerExecutionAnalyse = (projetId) =>
  api.post('/executions-analyse-qualite', null, {
    params: { projetId },
  });

/*
 * Lance réellement SonarQube
 */
export const executerAnalyse = (executionId) =>
  api.post(
    `/executions-analyse-qualite/${executionId}/executer`
  );

/*
 * Récupère les exécutions d'analyse d'un projet
 */
export const obtenirExecutionsAnalyseParProjet = (projetId) =>
  api.get(
    `/executions-analyse-qualite/projet/${projetId}`
  );

/*
 * Récupère les métriques Sonar d'une exécution
 */
export const obtenirAnalyseParExecution = (executionId) =>
  api.get(
    `/analyses-qualite/execution/${executionId}`
  );

/*
 * Supprime AnalyseQualite
 */
export const supprimerAnalyseQualite = (analyseId) =>
  api.delete(
    `/analyses-qualite/${analyseId}`
  );

/*
 * Supprime ExecutionAnalyseQualite
 */
export const supprimerExecutionAnalyse = (executionId) =>
  api.delete(
    `/executions-analyse-qualite/${executionId}`
  );
