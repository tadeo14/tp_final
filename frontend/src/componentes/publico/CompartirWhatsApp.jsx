/** Enlace para mandar la página actual por WhatsApp (wa.me abre la app en el celular o WhatsApp Web). */
export default function CompartirWhatsApp({ texto }) {
  const mensaje = `${texto}\n${window.location.href}`;
  return (
    <a className="boton-whatsapp" href={`https://wa.me/?text=${encodeURIComponent(mensaje)}`} target="_blank"
      rel="noopener noreferrer">
      Compartir por WhatsApp
    </a>
  );
}
