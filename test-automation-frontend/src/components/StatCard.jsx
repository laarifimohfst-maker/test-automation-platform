function StatCard({ titre, valeur, couleur, icone, fondIcone }) {
  return (
    <div style={{ backgroundColor: '#ffffff', borderRadius: '10px', padding: '16px', flex: 1 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: fondIcone, color: couleur || '#7c3aed', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{icone}</div>
        <div>
          <p style={{ margin: 0, color: '#6b7280', fontSize: '14px' }}>{titre}</p>
          <p style={{ margin: '4px 0 0 0', fontSize: '28px', fontWeight: 'bold', color: '#111827' }}>{valeur}</p>
        </div>
      </div>
    </div>
  );
}

export default StatCard;