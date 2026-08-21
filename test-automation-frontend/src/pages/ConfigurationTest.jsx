import { useState, useEffect } from 'react';
import { obtenirProjetsUtilisateur } from '../services/projetService';
import {
  obtenirConfigurationsParProjet,
  configurerTests,
  supprimerConfiguration,
} from '../services/configurationTestService';
import { Trash2 } from 'lucide-react';
import { useAlertDialog } from '../components/AlertDialogContext';
import './ConfigurationTest.css';

const UTILISATEUR_ID = 1; // en dur pour l'instant, comme le reste de l'app

function ConfigurationTest() {
  const { demanderConfirmation, afficherAlerte } = useAlertDialog();

  // --- Liste des projets pour le sélecteur ---
  const [projets, setProjets] = useState([]);
  const [projetSelectionneId, setProjetSelectionneId] = useState('');

  // --- Données liées au projet sélectionné ---
  const [configurations, setConfigurations] = useState([]);
  const [chargementConfigs, setChargementConfigs] = useState(false);

  // Cases à cocher du formulaire de nouvelle configuration
  const [testsUnitaires, setTestsUnitaires] = useState(true);
  const [testsIntegration, setTestsIntegration] = useState(false);
  const [testsApi, setTestsApi] = useState(false);

  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [succes, setSucces] = useState(false);

  // Chargement de la liste des projets au montage de la page
  useEffect(() => {
    obtenirProjetsUtilisateur(UTILISATEUR_ID)
      .then((res) => setProjets(res.data))
      .catch((err) => console.error('Erreur chargement projets', err));
  }, []);

  // Charge les configurations existantes à chaque fois qu'un autre projet est choisi
  const chargerConfigurations = (id) => {
    setChargementConfigs(true);
    obtenirConfigurationsParProjet(id)
      .then((res) => setConfigurations(res.data))
      .catch((err) => console.error('Erreur chargement configurations', err))
      .finally(() => setChargementConfigs(false));
  };

  useEffect(() => {
    if (!projetSelectionneId) {
      setConfigurations([]);
      return;
    }
    chargerConfigurations(projetSelectionneId);
    setErreur(null);
    setSucces(false);
  }, [projetSelectionneId]);

  const gererCreation = () => {
    if (!testsUnitaires && !testsIntegration && !testsApi) {
      setErreur('Sélectionne au moins un type de test.');
      return;
    }

    setEnCours(true);
    setErreur(null);
    setSucces(false);

    configurerTests(projetSelectionneId, { testsUnitaires, testsIntegration, testsApi })
      .then(() => {
        setSucces(true);
        chargerConfigurations(projetSelectionneId);
      })
      .catch((err) => {
        console.error('Erreur création configuration', err);
        setErreur(err.response?.data || 'Échec de la création de la configuration.');
      })
      .finally(() => setEnCours(false));
  };

  const gererSuppression = async (config) => {
    const confirme = await demanderConfirmation({
      titre: 'Supprimer la configuration ?',
      message:
        'Cette configuration de tests sera retirée définitivement du projet.',
      texteConfirmation: 'Supprimer',
    });

    if (!confirme) return;

    supprimerConfiguration(config.id)
      .then(() => chargerConfigurations(projetSelectionneId))
      .catch((err) => {
        console.error('Erreur suppression configuration', err);
        afficherAlerte({
          variante: 'error',
          titre: 'Suppression impossible',
          message: 'La configuration n’a pas pu être supprimée.',
          texteConfirmation: 'Fermer',
        });
      });
  };

  return (
    <div className="page">
      {/* --- Sélecteur de projet --- */}
      <div className="card">
        <h3 className="cardTitre">Choisir un projet</h3>
        <select
          className="select"
          value={projetSelectionneId}
          onChange={(e) => setProjetSelectionneId(e.target.value)}
        >
          <option value="">-- Sélectionne un projet --</option>
          {projets.map((p) => (
            <option key={p.id} value={p.id}>{p.nom}</option>
          ))}
        </select>
      </div>

      {/* --- Le reste ne s'affiche que si un projet est choisi --- */}
      {projetSelectionneId && (
        <>
          {/* --- Formulaire nouvelle configuration --- */}
          <div className="card">
            <h3 className="cardTitre">Nouvelle configuration</h3>

            <div className="optionLigne">
              <input
                type="checkbox"
                className="checkbox"
                checked={testsUnitaires}
                onChange={(e) => setTestsUnitaires(e.target.checked)}
              />
              <div>
                <div className="optionLabel">Tests unitaires</div>
                <div className="optionDescription">Exécutés via Surefire (JUnit 5 + Mockito)</div>
              </div>
            </div>

            <div className="optionLigne">
              <input
                type="checkbox"
                className="checkbox"
                checked={testsIntegration}
                onChange={(e) => setTestsIntegration(e.target.checked)}
              />
              <div>
                <div className="optionLabel">Tests d'intégration</div>
                <div className="optionDescription">Exécutés via Failsafe (classes *IT)</div>
              </div>
            </div>

            <div className="optionLigne">
              <input
                type="checkbox"
                className="checkbox"
                checked={testsApi}
                onChange={(e) => setTestsApi(e.target.checked)}
              />
              <div>
                <div className="optionLabel">Tests API</div>
                <div className="optionDescription">Exécutés via Failsafe + Rest Assured (classes *ApiIT)</div>
              </div>
            </div>

            <button className="boutonPrincipal" onClick={gererCreation} disabled={enCours}>
              {enCours ? 'Enregistrement...' : 'Enregistrer la configuration'}
            </button>

            {erreur && <div className="messageErreur">{erreur}</div>}
            {succes && <div className="messageSucces">Configuration enregistrée avec succès.</div>}
          </div>

          {/* --- Historique des configurations existantes --- */}
          <div className="card">
            <h3 className="cardTitre">Configurations existantes</h3>

            {chargementConfigs && <div className="emptyState">Chargement...</div>}

            {!chargementConfigs && configurations.length === 0 && (
              <div className="emptyState">Aucune configuration pour ce projet pour l'instant.</div>
            )}

            {!chargementConfigs && configurations.map((config) => (
              <div key={config.id} className="configLigne">
                <div className="configBadges">
                  {config.testsUnitaires && <span className="badgeType">Unitaires</span>}
                  {config.testsIntegration && <span className="badgeType">Intégration</span>}
                  {config.testsApi && <span className="badgeType">API</span>}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <span className="configDate">
                    {config.dateConfiguration
                      ? new Date(config.dateConfiguration).toLocaleString('fr-FR')
                      : '-'}
                  </span>
                  <Trash2 size={18} className="iconDelete" onClick={() => gererSuppression(config)} />
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default ConfigurationTest;
