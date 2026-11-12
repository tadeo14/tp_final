/** Muestra el error del backend tal cual: el mensaje y, si vienen, los detalles. */
export default function MensajeError({ error }) {
  if (!error) return null;
  return (
    <div className="aviso aviso-error" role="alert">
      <p>{error.message}</p>
      {error.detalles?.length > 0 && (
        <ul>
          {error.detalles.map((detalle, i) => (
            <li key={i}>{detalle}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
