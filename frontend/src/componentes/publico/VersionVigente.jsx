import { momento } from '../../formato.js';

/** Qué versión publicada se está mostrando y, si es una corrección, su motivo (D46, D47). */
export default function VersionVigente({ version }) {
  return (
    <div className={version.motivo ? 'aviso aviso-info' : 'version'}>
      <p>Versión {version.numero} · publicada el {momento(version.publicadaEn)}</p>
      {version.motivo && <p className="motivo">Corrección: {version.motivo}</p>}
    </div>
  );
}
