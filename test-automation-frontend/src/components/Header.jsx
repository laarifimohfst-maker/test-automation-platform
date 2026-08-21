import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState, useRef } from 'react';
import { Upload, GitBranch, X } from 'lucide-react';
import { importerProjetZip, importerProjetGithub } from '../services/projetService';
import NotificationBell from './NotificationBell';
import { obtenirUtilisateurParId } from '../services/utilisateurService';

const UTILISATEUR_ID = 1;

function Header() {
  const location = useLocation();
  const navigate = useNavigate();

  const estProjets = location.pathname === '/projets';
  const estConfiguration = location.pathname === '/configurations';
  const estExecutions = location.pathname === '/executions';
  const estResultats = location.pathname === '/resultats';
  const estQualite = location.pathname === '/qualite';
  const estProfil = location.pathname === '/profil';

  const [menuImport, setMenuImport] = useState(false);
  const [modaleGithub, setModaleGithub] = useState(false);
  const [urlGithub, setUrlGithub] = useState('');
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState(null);
  const [utilisateurHeader, setUtilisateurHeader] = useState(null);

  const inputFileRef = useRef(null);

  useEffect(() => {
    let composantActif = true;

    obtenirUtilisateurParId(UTILISATEUR_ID)
      .then((response) => {
        if (composantActif) setUtilisateurHeader(response.data);
      })
      .catch((err) => {
        console.error('Erreur chargement utilisateur du header :', err);
      });

    const actualiserUtilisateur = (event) => {
      if (event.detail) setUtilisateurHeader(event.detail);
    };

    window.addEventListener('profilMisAJour', actualiserUtilisateur);

    return () => {
      composantActif = false;
      window.removeEventListener('profilMisAJour', actualiserUtilisateur);
    };
  }, []);

  const initialesUtilisateur = () => {
    const morceaux = (utilisateurHeader?.nom || 'Utilisateur')
      .trim()
      .split(/\s+/)
      .filter(Boolean);

    return morceaux
      .slice(0, 2)
      .map((morceau) => morceau.charAt(0).toUpperCase())
      .join('');
  };

  const prevenirImportReussi = () => {
    window.dispatchEvent(new CustomEvent('projetImporte'));
  };

  const gererChoixFichier = (e) => {
    const fichier = e.target.files[0];

    if (!fichier) return;

    setEnCours(true);
    setErreur(null);

    importerProjetZip(fichier, UTILISATEUR_ID)
      .then(() => {
        setMenuImport(false);
        prevenirImportReussi();
      })
      .catch((err) => {
        console.error('Erreur import ZIP', err);

        const message =
          err.response?.data ||
          "Échec de l'import du fichier ZIP.";

        setErreur(message);
      })
      .finally(() => {
        setEnCours(false);
        e.target.value = '';
      });
  };

  const gererImportGithub = () => {
    if (!urlGithub.trim()) return;

    setEnCours(true);
    setErreur(null);

    importerProjetGithub(
      urlGithub.trim(),
      UTILISATEUR_ID
    )
      .then(() => {
        setModaleGithub(false);
        setMenuImport(false);
        setUrlGithub('');
        prevenirImportReussi();
      })
      .catch((err) => {
        console.error('Erreur import GitHub', err);

        const message =
          err.response?.data ||
          'Échec du clonage du dépôt GitHub.';

        setErreur(message);
      })
      .finally(() => setEnCours(false));
  };

  return (
    <header
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '10px 20px',
      }}
    >

      {/* =========================
          TITRE DE LA PAGE
      ========================= */}

      <div>

        {estProjets ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Mes projets
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Gérez et consultez tous vos projets
            </p>
          </>

        ) : estConfiguration ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Configuration des tests
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Choisissez les types de tests à exécuter
            </p>
          </>

        ) : estExecutions ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Exécutions
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Lancez une configuration de tests enregistrée pour un projet précis
            </p>
          </>

        ) : estResultats ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Résultats des tests
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Consultez les détails des résultats d'une exécution de tests
            </p>
          </>

        ) : estQualite ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Analyse de qualité
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Lancez et consultez les analyses de qualité de vos projets
            </p>
          </>

        ) : estProfil ? (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
                fontSize: '22px',
              }}
            >
              Mon profil
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Consultez et modifiez vos informations personnelles
            </p>
          </>

        ) : (
          <>
            <h2
              style={{
                margin: 0,
                color: '#111827',
              }}
            >
              Bonjour 👋
            </h2>

            <p
              style={{
                margin: '4px 0 0',
                color: '#6b7280',
              }}
            >
              Voici un aperçu de vos projets et exécutions.
            </p>
          </>
        )}

      </div>

      {/* =========================
          PARTIE DROITE
      ========================= */}

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
        }}
      >

        {/* Bouton Nouveau projet uniquement sur la page Mes projets */}
        {estProjets && (
          <div style={{ position: 'relative' }}>

            <button
              onClick={() =>
                setMenuImport(!menuImport)
              }
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '7px',
                padding: '9px 16px',
                border: 'none',
                borderRadius: '7px',
                backgroundColor: '#0284c7',
                color: '#fff',
                cursor: 'pointer',
                fontWeight: '600',
                fontSize: '14px',
              }}
            >
              + Nouveau projet
            </button>

            {menuImport && (
              <div
                style={{
                  position: 'absolute',
                  top: '44px',
                  right: 0,
                  width: '250px',
                  backgroundColor: '#fff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                  zIndex: 100,
                }}
              >

                {/* Header menu */}
                <div
                  style={{
                    padding: '12px 15px',
                    borderBottom: '1px solid #f3f4f6',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <span
                    style={{
                      fontWeight: '600',
                      color: '#111827',
                      fontSize: '13px',
                    }}
                  >
                    Importer un projet
                  </span>

                  <X
                    size={16}
                    color="#9ca3af"
                    style={{ cursor: 'pointer' }}
                    onClick={() =>
                      setMenuImport(false)
                    }
                  />
                </div>

                {/* Import ZIP */}
                <button
                  onClick={() =>
                    inputFileRef.current.click()
                  }
                  disabled={enCours}
                  style={{
                    width: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    padding: '13px 15px',
                    border: 'none',
                    backgroundColor: '#fff',
                    cursor: enCours
                      ? 'not-allowed'
                      : 'pointer',
                    textAlign: 'left',
                  }}
                >

                  <Upload
                    size={19}
                    color="#0284c7"
                  />

                  <div>

                    <div
                      style={{
                        fontWeight: '600',
                        color: '#111827',
                        fontSize: '13px',
                      }}
                    >
                      Importer un fichier ZIP
                    </div>

                    <div
                      style={{
                        color: '#9ca3af',
                        fontSize: '11px',
                        marginTop: '3px',
                      }}
                    >
                      Depuis votre ordinateur
                    </div>

                  </div>
                </button>

                <input
                  ref={inputFileRef}
                  type="file"
                  accept=".zip"
                  onChange={gererChoixFichier}
                  style={{ display: 'none' }}
                />

                {/* Import GitHub */}
                <button
                  onClick={() => {
                    setModaleGithub(true);
                    setMenuImport(false);
                  }}
                  style={{
                    width: '100%',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    padding: '13px 15px',
                    border: 'none',
                    borderTop: '1px solid #f3f4f6',
                    backgroundColor: '#fff',
                    cursor: 'pointer',
                    textAlign: 'left',
                  }}
                >

                  <GitBranch
                    size={19}
                    color="#374151"
                  />

                  <div>

                    <div
                      style={{
                        fontWeight: '600',
                        color: '#111827',
                        fontSize: '13px',
                      }}
                    >
                      Cloner depuis GitHub
                    </div>

                    <div
                      style={{
                        color: '#9ca3af',
                        fontSize: '11px',
                        marginTop: '3px',
                      }}
                    >
                      Depuis un dépôt GitHub
                    </div>

                  </div>
                </button>

                {erreur && (
                  <div
                    style={{
                      padding: '10px 15px',
                      color: '#dc2626',
                      fontSize: '12px',
                    }}
                  >
                    {erreur}
                  </div>
                )}

              </div>
            )}

          </div>
        )}

        {/* Notifications */}
        <NotificationBell />

        {/* Profil */}
        <button
          type="button"
          className="header-profile-button"
          aria-label="Ouvrir mon profil"
          onClick={() => navigate('/profil')}
          title={utilisateurHeader?.nom || 'Utilisateur'}
          style={{
            width: '36px',
            height: '36px',
            padding: 0,
            border: 'none',
            borderRadius: '50%',
            backgroundColor: '#0ea5e9',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: '600',
            fontSize: '13px',
            cursor: 'pointer',
          }}
        >
          {initialesUtilisateur()}
        </button>

      </div>

      {/* =========================
          MODALE GITHUB
      ========================= */}

      {modaleGithub && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 200,
          }}
        >

          <div
            style={{
              backgroundColor: '#fff',
              borderRadius: '10px',
              padding: '24px',
              width: '400px',
            }}
          >

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '16px',
              }}
            >

              <h3
                style={{
                  margin: 0,
                  fontSize: '16px',
                }}
              >
                Cloner depuis GitHub
              </h3>

              <X
                size={18}
                style={{
                  cursor: 'pointer',
                }}
                onClick={() =>
                  setModaleGithub(false)
                }
              />

            </div>

            <input
              type="text"
              placeholder="https://github.com/utilisateur/depot.git"
              value={urlGithub}
              onChange={(e) =>
                setUrlGithub(e.target.value)
              }
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '6px',
                border: '1px solid #e5e7eb',
                fontSize: '14px',
                boxSizing: 'border-box',
                marginBottom: '12px',
              }}
            />

            {erreur && (
              <div
                style={{
                  color: '#dc2626',
                  fontSize: '13px',
                  marginBottom: '12px',
                }}
              >
                {erreur}
              </div>
            )}

            <button
              onClick={gererImportGithub}
              disabled={
                enCours ||
                !urlGithub.trim()
              }
              style={{
                width: '100%',
                padding: '10px',
                border: 'none',
                borderRadius: '6px',
                backgroundColor: '#0284c7',
                color: '#fff',
                fontWeight: '600',
                cursor: enCours
                  ? 'not-allowed'
                  : 'pointer',
              }}
            >
              {enCours
                ? 'Clonage en cours...'
                : 'Importer'}
            </button>

          </div>

        </div>
      )}

    </header>
  );
}

export default Header;
