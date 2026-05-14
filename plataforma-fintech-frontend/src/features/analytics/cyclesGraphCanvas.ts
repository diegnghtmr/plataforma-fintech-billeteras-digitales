import type { NodeObject } from 'react-force-graph-2d';
import {
  resolveNodeStyle,
  GRAPH_TOKENS,
  type CycleGraphNode,
  type DrawNodeOpts,
  type DrawLinkOpts,
  type DrawBackgroundOpts,
} from './cyclesGraphUtils';

// ---------------------------------------------------------------------------
// Private helpers
// ---------------------------------------------------------------------------

function resolveCoord(x: string | NodeObject | number | undefined): { x: number; y: number } {
  if (x != null && typeof x === 'object' && 'x' in x) {
    const node = x as NodeObject;
    return { x: node.x ?? 0, y: node.y ?? 0 };
  }
  return { x: 0, y: 0 };
}

// ---------------------------------------------------------------------------
// drawNode — distill.pub-style: pale grey disc, thin dark stroke, plain text
// label below (no pill background). Selected = cobalt fill; neighbor = pale
// cobalt tint. The accent appears only on the focused ego.
// ---------------------------------------------------------------------------

export function drawNode(
  ctx: CanvasRenderingContext2D,
  node: CycleGraphNode,
  opts: DrawNodeOpts,
): void {
  const { state, opacity } = opts;
  const style = resolveNodeStyle(state);
  const nx = node.x ?? 0;
  const ny = node.y ?? 0;

  ctx.save();
  ctx.globalAlpha = opacity;

  // Main disc — flat fill, thin dark stroke
  ctx.beginPath();
  ctx.arc(nx, ny, style.radius, 0, 2 * Math.PI);
  ctx.fillStyle = style.fill;
  ctx.fill();
  ctx.strokeStyle = style.stroke;
  ctx.lineWidth = style.strokeWidth;
  ctx.stroke();

  // Label — plain text below the node (no pill, just glyphs on canvas)
  ctx.font = GRAPH_TOKENS.node.label.font;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'top';
  const labelY = ny + style.radius + 6;
  ctx.fillStyle = style.labelColor;
  ctx.fillText(node.id, nx, labelY);

  ctx.restore();
}

// ---------------------------------------------------------------------------
// drawArrowhead — small filled triangle. Distill keeps these tiny so the
// direction reads without the arrow becoming visual furniture.
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
// drawLink — 1px solid line, small arrowhead, dark grey by default
// ---------------------------------------------------------------------------

export function drawLink(
  ctx: CanvasRenderingContext2D,
  source: { x: number; y: number } | string | NodeObject,
  target: { x: number; y: number } | string | NodeObject,
  opts: DrawLinkOpts,
): void {
  const { opacity, width, color } = opts;

  const srcPos =
    typeof source === 'object' && 'x' in source && typeof (source as { x: unknown }).x === 'number'
      ? (source as { x: number; y: number })
      : resolveCoord(source as string | NodeObject | number | undefined);

  const tgtPos =
    typeof target === 'object' && 'x' in target && typeof (target as { x: unknown }).x === 'number'
      ? (target as { x: number; y: number })
      : resolveCoord(target as string | NodeObject | number | undefined);

  // Trim by ~radius so the line meets the node edge, not its center.
  const dx = tgtPos.x - srcPos.x;
  const dy = tgtPos.y - srcPos.y;
  const dist = Math.sqrt(dx * dx + dy * dy) || 1;
  const trim = GRAPH_TOKENS.node.radius + 2;
  const ux = dx / dist;
  const uy = dy / dist;
  const x1 = srcPos.x + ux * trim;
  const y1 = srcPos.y + uy * trim;
  const x2 = tgtPos.x - ux * trim;
  const y2 = tgtPos.y - uy * trim;

  ctx.save();
  ctx.globalAlpha = opacity;
  ctx.strokeStyle = color;
  ctx.lineWidth = width;
  ctx.lineCap = 'butt'; // distill uses crisp ends, not rounded caps
  ctx.setLineDash([]);

  ctx.beginPath();
  ctx.moveTo(x1, y1);
  ctx.lineTo(x2, y2);
  ctx.stroke();

  // Small arrowhead at the trimmed target end
  const angle = Math.atan2(dy, dx);
  drawArrowhead(ctx, x2, y2, angle, GRAPH_TOKENS.edge.arrowSize, color);

  ctx.restore();
}

// ---------------------------------------------------------------------------
// drawDotBackground — kept for API compatibility; renders nothing.
// Distill backgrounds are flat — the card provides the surface.
// ---------------------------------------------------------------------------

export function drawDotBackground(
  _ctx: CanvasRenderingContext2D,
  _canvasWidth: number,
  _canvasHeight: number,
  _opts: DrawBackgroundOpts,
): void {
  // Intentionally a no-op — flat background is part of the distill aesthetic.
}
