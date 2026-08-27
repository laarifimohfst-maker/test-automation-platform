import { Navigate, Route, Routes } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Dashboard from './pages/Dashboard';
import Projets from './pages/Projets';
import ConfigurationTest from './pages/ConfigurationTest';
import Executions from './pages/Executions';
import ResultatsTests from './pages/ResultatsTests';
import AnalyseQualite from './pages/AnalyseQualite';
import Profil from './pages/Profil';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminAccueil from './pages/AdminAccueil';
import useAuth from './hooks/useAuth';
import { AlertDialogProvider } from './components/AlertDialog';

function RouteProtegee({ children, role }) {
  const { utilisateur } = useAuth();

  if (!utilisateur) {
    return <Navigate to="/login" replace />;
  }

  if (role && utilisateur.role !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function ApplicationDeveloppeur() {
  return (
    <AlertDialogProvider>
      <div className="app-shell">
        <Sidebar />
        <div className="app-content">
          <Header />
          <main className="app-main">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/projets" element={<Projets />} />
              <Route path="/configurations" element={<ConfigurationTest />} />
              <Route path="/executions" element={<Executions />} />
              <Route path="/resultats" element={<ResultatsTests />} />
              <Route path="/qualite" element={<AnalyseQualite />} />
              <Route path="/profil" element={<Profil />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </div>
    </AlertDialogProvider>
  );
}

function App() {
  const { utilisateur } = useAuth();

  return (
    <Routes>
      <Route
        path="/login"
        element={
          utilisateur ? (
            <Navigate
              to={utilisateur.role === 'ADMIN' ? '/admin' : '/'}
              replace
            />
          ) : (
            <Login />
          )
        }
      />
      <Route
        path="/register"
        element={
          utilisateur ? (
            <Navigate
              to={utilisateur.role === 'ADMIN' ? '/admin' : '/'}
              replace
            />
          ) : (
            <Register />
          )
        }
      />
      <Route
        path="/admin"
        element={
          <RouteProtegee role="ADMIN">
            <AdminAccueil />
          </RouteProtegee>
        }
      />
      <Route
        path="/*"
        element={
          <RouteProtegee>
            <ApplicationDeveloppeur />
          </RouteProtegee>
        }
      />
    </Routes>
  );
}

export default App;
