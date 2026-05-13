import { describe, it, expect } from 'vitest';
import { queryKeys } from '../query-keys';

describe('queryKeys', () => {
  it('users.all equals ["users"]', () => {
    expect(queryKeys.users.all).toEqual(['users']);
  });

  it('wallets.byUser returns ["wallets", userId]', () => {
    expect(queryKeys.wallets.byUser('USR001')).toEqual(['wallets', 'USR001']);
  });

  it('transactions.byUser with no filters returns ["transactions", "user", userId, {}]', () => {
    expect(queryKeys.transactions.byUser('USR001', undefined)).toEqual([
      'transactions',
      'user',
      'USR001',
      {},
    ]);
  });

  it('analytics.summary equals ["analytics", "summary"]', () => {
    expect(queryKeys.analytics.summary).toEqual(['analytics', 'summary']);
  });

  it('fraud.events() called twice with same arg produces same deep-equal result', () => {
    const a = queryKeys.fraud.events();
    const b = queryKeys.fraud.events();
    expect(a).toEqual(b);
  });

  // T08-F01: scheduled operations query keys
  it('scheduledOperations.all equals ["scheduledOperations"]', () => {
    expect(queryKeys.scheduledOperations.all).toEqual(['scheduledOperations']);
  });

  // T08-F01: notifications query keys
  it('notifications.byUser returns key with userId and unreadOnly', () => {
    expect(queryKeys.notifications.byUser('USR001', false)).toEqual([
      'notifications', 'byUser', 'USR001', false,
    ]);
  });

  it('notifications.byUser with unreadOnly=true includes true in key', () => {
    expect(queryKeys.notifications.byUser('USR001', true)).toEqual([
      'notifications', 'byUser', 'USR001', true,
    ]);
  });

  it('notifications.byUser with no unreadOnly defaults to false', () => {
    expect(queryKeys.notifications.byUser('USR001')).toEqual([
      'notifications', 'byUser', 'USR001', false,
    ]);
  });

  // T07-F01: points query keys
  it('points.byUser returns ["points", userId]', () => {
    expect(queryKeys.points.byUser('u1')).toEqual(['points', 'u1']);
  });

  it('points.ranking() with no arg returns ["points","ranking",10]', () => {
    expect(queryKeys.points.ranking()).toEqual(['points', 'ranking', 10]);
  });

  it('points.ranking(25) returns ["points","ranking",25]', () => {
    expect(queryKeys.points.ranking(25)).toEqual(['points', 'ranking', 25]);
  });
});
