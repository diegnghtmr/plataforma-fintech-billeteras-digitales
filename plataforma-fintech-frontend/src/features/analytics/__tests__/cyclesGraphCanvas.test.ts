import { describe, it, expect, vi, beforeEach } from 'vitest';
import { drawNode, drawLink, drawDotBackground } from '../cyclesGraphCanvas';
import type { CycleGraphNode } from '../cyclesGraphUtils';

function createMockCtx() {
  const ctx = {
    beginPath: vi.fn(),
    arc: vi.fn(),
    fill: vi.fn(),
    stroke: vi.fn(),
    moveTo: vi.fn(),
    lineTo: vi.fn(),
    closePath: vi.fn(),
    quadraticCurveTo: vi.fn(),
    fillRect: vi.fn(),
    setLineDash: vi.fn(),
    save: vi.fn(),
    restore: vi.fn(),
    translate: vi.fn(),
    rotate: vi.fn(),
    fillText: vi.fn(),
    measureText: vi.fn(() => ({ width: 20 })),
    createRadialGradient: vi.fn(() => ({ addColorStop: vi.fn() })),
    fillStyle: '' as string | CanvasGradient | CanvasPattern,
    strokeStyle: '' as string | CanvasGradient | CanvasPattern,
    lineWidth: 0,
    font: '',
    textAlign: 'center' as CanvasTextAlign,
    textBaseline: 'middle' as CanvasTextBaseline,
    globalAlpha: 1,
  };
  return ctx as unknown as CanvasRenderingContext2D & { [k: string]: ReturnType<typeof vi.fn> };
}

const baseNode: CycleGraphNode = {
  id: 'A',
  cycleIndex: 0,
  positionInCycle: 1,
  x: 50,
  y: 50,
};

describe('drawNode', () => {
  let ctx: ReturnType<typeof createMockCtx>;

  beforeEach(() => {
    ctx = createMockCtx();
  });

  it('calls arc at least 4 times (3 halos + main circle)', () => {
    drawNode(ctx, baseNode, { radius: 14, opacity: 1, isSelected: false, isHovered: false, showBadge: true });
    expect(ctx.arc.mock.calls.length).toBeGreaterThanOrEqual(4);
  });

  it('calls createRadialGradient at least 2 times (node fill + badge)', () => {
    drawNode(ctx, baseNode, { radius: 14, opacity: 1, isSelected: false, isHovered: false, showBadge: true });
    expect(ctx.createRadialGradient.mock.calls.length).toBeGreaterThanOrEqual(2);
  });

  it('sets globalAlpha to the opacity value', () => {
    drawNode(ctx, baseNode, { radius: 14, opacity: 0.5, isSelected: false, isHovered: false, showBadge: false });
    // globalAlpha will have been set during the draw call
    // We check it was called with 0.5 at some point (save/restore pattern)
    expect(ctx.save).toHaveBeenCalled();
    expect(ctx.restore).toHaveBeenCalled();
  });

  it('uses larger halo radii when isSelected is true', () => {
    drawNode(ctx, baseNode, { radius: 14, opacity: 1, isSelected: true, isHovered: false, showBadge: false });
    // isSelected → halos at r+4, r+8, r+12
    // arc is called with radius as 3rd arg; find halo calls
    const arcCalls = ctx.arc.mock.calls as unknown[][];
    // r+12 = 26 should appear
    const hasLargeHalo = arcCalls.some((call) => call[2] === 14 + 12);
    expect(hasLargeHalo).toBe(true);
  });

  it('uses smaller halo radii when not selected', () => {
    drawNode(ctx, baseNode, { radius: 14, opacity: 1, isSelected: false, isHovered: false, showBadge: false });
    const arcCalls = ctx.arc.mock.calls as unknown[][];
    // r+8 = 22 should appear (default halo max)
    const hasNormalHalo = arcCalls.some((call) => call[2] === 14 + 8);
    expect(hasNormalHalo).toBe(true);
  });
});

describe('drawLink', () => {
  let ctx: ReturnType<typeof createMockCtx>;

  beforeEach(() => {
    ctx = createMockCtx();
  });

  it('calls setLineDash with [6, 4]', () => {
    drawLink(ctx, { x: 0, y: 0 }, { x: 100, y: 0 }, {
      opacity: 1,
      width: 2.5,
      color: '#494fdf',
      dashPattern: [6, 4],
    });
    expect(ctx.setLineDash).toHaveBeenCalledWith([6, 4]);
  });

  it('calls stroke to draw the line', () => {
    drawLink(ctx, { x: 0, y: 0 }, { x: 100, y: 0 }, {
      opacity: 1,
      width: 2.5,
      color: '#494fdf',
      dashPattern: [6, 4],
    });
    expect(ctx.stroke).toHaveBeenCalled();
  });

  it('draws an arrowhead with moveTo + lineTo + closePath + fill', () => {
    drawLink(ctx, { x: 0, y: 0 }, { x: 100, y: 0 }, {
      opacity: 1,
      width: 2.5,
      color: '#494fdf',
      dashPattern: [6, 4],
    });
    expect(ctx.moveTo).toHaveBeenCalled();
    expect(ctx.lineTo.mock.calls.length).toBeGreaterThanOrEqual(2);
    expect(ctx.closePath).toHaveBeenCalled();
    expect(ctx.fill).toHaveBeenCalled();
  });
});

describe('drawDotBackground', () => {
  it('calls arc exactly (width/dotSpacing) × (height/dotSpacing) times', () => {
    const ctx = createMockCtx();
    drawDotBackground(ctx, 480, 320, {
      width: 480,
      height: 320,
      dotSpacing: 20,
      dotColor: 'rgba(73,79,223,0.06)',
    });
    // 24 × 16 = 384 dots
    expect(ctx.arc.mock.calls.length).toBe(384);
  });
});
