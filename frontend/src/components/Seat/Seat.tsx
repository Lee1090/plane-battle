import type { PlayerSide } from '../../types/game';

interface SeatProps {
  label: string;
  side: PlayerSide;
  occupied: boolean;
  canSit: boolean;
  availableText: string;
  occupiedText: string;
  sitDownText: string;
  onSitDown: (side: PlayerSide) => void;
}

export function Seat({ label, side, occupied, canSit, availableText, occupiedText, sitDownText, onSitDown }: SeatProps) {
  return (
    <section className={`seat ${occupied ? 'seatOccupied' : ''}`} aria-label={label}>
      <div>
        <h2>{label}</h2>
        <p>{occupied ? occupiedText : availableText}</p>
      </div>
      <button type="button" disabled={!canSit || occupied} onClick={() => onSitDown(side)}>
        {sitDownText}
      </button>
    </section>
  );
}
