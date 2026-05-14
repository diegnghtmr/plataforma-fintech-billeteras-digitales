import type { NodeObject } from 'react-force-graph-2d';
import type { CycleGraphNode, DrawNodeOpts, DrawLinkOpts, DrawBackgroundOpts } from './cyclesGraphUtils';

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

function resolveId(x: string | NodeObject | number | undefined): string {
  if (typeof x === 'string') return x;
  if (typeof x === 'number') return String(x);
  if (x != null && typeof x === 'object' && 'id' in x) return String((x as NodeObject).id);
  return '';
}

function resolveCoord(x: string | NodeObject | number | undefined): { x: number; y: number } {
  if (x != null && typeof x === 'object' && 'x' in x) {
    const node = x as NodeObject;
    return { x: node.x ?? 0, y: node.y ?? 0 };
  }
  return { x: 0, y: 0 };
}

// ---------------------------------------------------------------------------
// drawNode
// ---------------------------------------------------------------------------

export function drawNode(
  ctx: CanvasRenderingContext2D,
  node: CycleGraphNode,
  opts: DrawNodeOpts,
): void {
  const { radius, opacity, isSelected, isHovered, showBadge } = opts;
  const nx = node.x ?? 0;
  const ny = node.y ?? 0;

  ctx.save();
  ctx.globalAlpha = opacity;

  // Halo radii: expanded when selected or hovered
  const haloActive = isSelected || isHovered;
  const haloOffsets = haloActive ? [4, 8, 12] : [2, 4, 8];
  const haloAlpha = haloActive ? 0.14 : 0.08;

  for (const offset of haloOffsets) {
    ctx.beginPath();
    ctx.arc(nx, ny, radius + offset, 0, 2 * Math.PI);
    ctx.strokeStyle = `rgba(73,79,223,${haloAlpha})`;
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }

  // Node fill — radial gradient white → #f4f4f4
  const grad = ctx.createRadialGradient(nx, ny, 0, nx, ny, radius);
  grad.addColorStop(0, '#ffffff');
  grad.addColorStop(1, '#f4f4f4');

  ctx.beginPath();
  ctx.arc(nx, ny, radius, 0, 2 * Math.PI);
  ctx.fillStyle = grad;
  ctx.fill();

  // 2px cobalt border
  ctx.strokeStyle = '#494fdf';
  ctx.lineWidth = 2;
  ctx.stroke();

  // Position badge
  if (showBadge) {
    const bx = nx + radius * 0.7;
    const by = ny - radius * 0.7;
    const br = 11; // 22px diameter

    const badgeGrad = ctx.createRadialGradient(bx, by, 0, bx, by, br);
    badgeGrad.addColorStop(0, '#4f55f1');
    badgeGrad.addColorStop(0.6, '#494fdf');
    badgeGrad.addColorStop(1, '#3a40c4');

    ctx.beginPath();
    ctx.arc(bx, by, br, 0, 2 * Math.PI);
    ctx.fillStyle = badgeGrad;
    ctx.fill();

    // White border
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    // Badge number
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 9px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(String(node.positionInCycle), bx, by);
  }

  ctx.restore();
}

// ---------------------------------------------------------------------------
// drawArrowhead
// ---------------------------------------------------------------------------

function drawArrowhead(
  ctx: CanvasRenderingContext2D,
  tipX: number,
  tipY: number,
  angle: number,
  size: number,
  color: string,
): void {
  ctx.save();
  ctx.translate(tipX, tipY);
  ctx.rotate(angle);
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(-size, -size / 2);
  ctx.lineTo(-size, size / 2);
  ctx.closePath();
  ctx.fillStyle = color;
  ctx.fill();
  ctx.restore();
}

// ---------------------------------------------------------------------------
// drawLink
// ---------------------------------------------------------------------------

export function drawLink(
  ctx: CanvasRenderingContext2D,
  source: { x: number; y: number } | string | NodeObject,
  target: { x: number; y: number } | string | NodeObject,
  opts: DrawLinkOpts,
): void {
  const { opacity, width, color, dashPattern } = opts;

  const srcPos =
    typeof source === 'object' && 'x' in source && typeof (source as { x: unknown }).x === 'number'
      ? (source as { x: number; y: number })
      : resolveCoord(source as string | NodeObject | number | undefined);

  const tgtPos =
    typeof target === 'object' && 'x' in target && typeof (target as { x: unknown }).x === 'number'
      ? (target as { x: number; y: number })
      : resolveCoord(target as string | NodeObject | number | undefined);

  ctx.save();
  ctx.globalAlpha = opacity;
  ctx.strokeStyle = color;
  ctx.lineWidth = width;
  ctx.setLineDash(dashPattern);

  ctx.beginPath();
  ctx.moveTo(srcPos.x, srcPos.y);
  ctx.lineTo(tgtPos.x, tgtPos.y);
  ctx.stroke();

  // Arrow at the target end
  const dx = tgtPos.x - srcPos.x;
  const dy = tgtPos.y - srcPos.y;
  const angle = Math.atan2(dy, dx);
  const arrowSize = 8;

  ctx.setLineDash([]);
  drawArrowhead(ctx, tgtPos.x, tgtPos.y, angle, arrowSize, color);

  ctx.restore();
}

// ---------------------------------------------------------------------------
// drawDotBackground
// ---------------------------------------------------------------------------

export function drawDotBackground(
  ctx: CanvasRenderingContext2D,
  _canvasWidth: number,
  _canvasHeight: number,
  opts: DrawBackgroundOpts,
): void {
  const { width, height, dotSpacing, dotColor } = opts;
  const cols = Math.floor(width / dotSpacing);
  const rows = Math.floor(height / dotSpacing);

  ctx.save();
  ctx.fillStyle = dotColor;

  for (let col = 0; col < cols; col++) {
    for (let row = 0; row < rows; row++) {
      const x = col * dotSpacing + dotSpacing / 2;
      const y = row * dotSpacing + dotSpacing / 2;
      ctx.beginPath();
      ctx.arc(x, y, 1.2, 0, 2 * Math.PI);
      ctx.fill();
    }
  }

  ctx.restore();
}
