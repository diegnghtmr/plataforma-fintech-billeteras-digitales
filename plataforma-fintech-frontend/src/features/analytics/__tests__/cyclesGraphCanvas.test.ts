import { describe, it, expect, vi, beforeEach, type MockInstance } from 'vitest';
import { drawNode, drawLink, drawDotBackground } from '../cyclesGraphCanvas';
import { GRAPH_TOKENS, type CycleGraphNode } from '../cyclesGraphUtils';

type MockCtx = {
  beginPath: MockInstance;
  arc: MockInstance;
  arcTo: MockInstance;
  fill: MockInstance;
  stroke: MockInstance;
  moveTo: MockInstance;
  lineTo: MockInstance;
  closePath: MockInstance;
  setLineDash: MockInstance;
  save: MockInstance;
  restore: MockInstance;
  translate: MockInstance;
  rotate: MockInstance;
  fillText: MockInstance;
  measureText: MockInstance;
  fillStyle: string;
  strokeStyle: string;
  lineWidth: number;
  lineCap: CanvasLineCap;
  font: string;
  textAlign: CanvasTextAlign;
  textBaseline: CanvasTextBaseline;
  globalAlpha: number;
};

function createMockCtx(): MockCtx {
  return {
    beginPath: vi.fn(),
    arc: vi.fn(),
    arcTo: vi.fn(),
    fill: vi.fn(),
    stroke: vi.fn(),
    moveTo: vi.fn(),
    lineTo: vi.fn(),
    closePath: vi.fn(),
    setLineDash: vi.fn(),
    save: vi.fn(),
    restore: vi.fn(),
    translate: vi.fn(),
    rotate: vi.fn(),
    fillText: vi.fn(),
    measureText: vi.fn(() => ({ width: 20 })),
    fillStyle: '',
    strokeStyle: '',
    lineWidth: 0,
    lineCap: 'butt' as CanvasLineCap,
    font: '',
    textAlign: 'center' as CanvasTextAlign,
    textBaseline: 'middle' as CanvasTextBaseline,
    globalAlpha: 1,
  };
}

const baseNode: CycleGraphNode = {
  id: 'USR001',
  cycleIndex: 0,
  positionInCycle: 1,
  x: 50,
  y: 50,
};

function asCtx(ctx: MockCtx): CanvasRenderingContext2D {
  return ctx as unknown as CanvasRenderingContext2D;
}

describe('drawNode', () => {
  let ctx: MockCtx;

  beforeEach(() => {
    ctx = createMockCtx();
  });

  it('draws a flat disc — exactly one arc + fill + stroke', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'default', opacity: 1 });
    // Distill aesthetic: a single disc, not a halo+rim+ring stack
    expect(ctx.arc.mock.calls.length).toBe(1);
    expect(ctx.fill).toHaveBeenCalled();
    expect(ctx.stroke).toHaveBeenCalled();
  });

  it('renders the node id as plain text via fillText (no pill background)', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'default', opacity: 1 });
    expect(ctx.fillText).toHaveBeenCalled();
    const fillTextCall = ctx.fillText.mock.calls[0] as [string, number, number];
    expect(fillTextCall[0]).toBe('USR001');
    // No rounded-rect pill ⇒ no arcTo calls
    expect(ctx.arcTo).not.toHaveBeenCalled();
  });

  it('does not call measureText — label is not pill-sized any more', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'default', opacity: 1 });
    expect(ctx.measureText).not.toHaveBeenCalled();
  });

  it('still draws exactly one disc when selected (no halo)', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'selected', opacity: 1 });
    // Selected differs by colour + radius, not by extra arcs
    expect(ctx.arc.mock.calls.length).toBe(1);
  });

  it('uses the distill label font for the text', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'default', opacity: 1 });
    expect(ctx.font).toBe(GRAPH_TOKENS.node.label.font);
  });

  it('wraps drawing with save/restore so opacity stays local', () => {
    drawNode(asCtx(ctx), baseNode, { state: 'dimmed', opacity: 0.2 });
    expect(ctx.save).toHaveBeenCalled();
    expect(ctx.restore).toHaveBeenCalled();
  });
});

describe('drawLink', () => {
  let ctx: MockCtx;

  beforeEach(() => {
    ctx = createMockCtx();
  });

  it('draws a solid (no-dash) line', () => {
    drawLink(asCtx(ctx), { x: 0, y: 0 }, { x: 100, y: 0 }, {
      opacity: 1,
      width: 1,
      color: GRAPH_TOKENS.edge.strokeDefault,
    });
    expect(ctx.setLineDash).toHaveBeenCalledWith([]);
    expect(ctx.stroke).toHaveBeenCalled();
  });

  it('draws an arrowhead with moveTo + lineTo + closePath + fill', () => {
    drawLink(asCtx(ctx), { x: 0, y: 0 }, { x: 100, y: 0 }, {
      opacity: 1,
      width: 1,
      color: GRAPH_TOKENS.edge.strokeDefault,
    });
    expect(ctx.moveTo).toHaveBeenCalled();
    expect(ctx.lineTo.mock.calls.length).toBeGreaterThanOrEqual(2);
    expect(ctx.closePath).toHaveBeenCalled();
    expect(ctx.fill).toHaveBeenCalled();
  });
});

describe('drawDotBackground', () => {
  it('is a no-op in the current visual language (flat background)', () => {
    const ctx = createMockCtx();
    drawDotBackground(asCtx(ctx), 480, 320, {
      width: 480,
      height: 320,
      dotSpacing: 20,
      dotColor: 'rgba(73,79,223,0.06)',
    });
    expect(ctx.arc.mock.calls.length).toBe(0);
    expect(ctx.fill).not.toHaveBeenCalled();
  });
});
