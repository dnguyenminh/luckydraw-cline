/**
 * Array manipulation utilities
 */

/**
 * Create chunks of an array with specified size
 */
export function chunk<T>(array: T[], size: number): T[][] {
  return Array.from({ length: Math.ceil(array.length / size) }, (_, index) =>
    array.slice(index * size, (index + 1) * size)
  );
}

/**
 * Get unique elements from an array
 */
export function unique<T>(array: T[]): T[] {
  return Array.from(new Set(array));
}

/**
 * Group array elements by a key
 */
export function groupBy<T>(array: T[], key: keyof T): Record<string, T[]> {
  return array.reduce((groups, item) => {
    const group = String(item[key]);
    groups[group] = groups[group] || [];
    groups[group].push(item);
    return groups;
  }, {} as Record<string, T[]>);
}

/**
 * Create an object composed of picked object properties
 */
export function pick<T extends object, K extends keyof T>(
  obj: T,
  keys: K[]
): Pick<T, K> {
  return keys.reduce((acc, key) => {
    if (key in obj) acc[key] = obj[key];
    return acc;
  }, {} as Pick<T, K>);
}

/**
 * Create an object composed of omitted object properties
 */
export function omit<T extends object, K extends keyof T>(
  obj: T,
  keys: K[]
): Omit<T, K> {
  const result = { ...obj };
  keys.forEach(key => delete result[key]);
  return result;
}

/**
 * Check if an array is empty
 */
export function isEmpty<T>(array: T[] | undefined | null): boolean {
  return !array || array.length === 0;
}

/**
 * Sort an array by a property key
 */
export function sortBy<T>(
  array: T[],
  key: keyof T,
  direction: 'asc' | 'desc' = 'asc'
): T[] {
  return [...array].sort((a, b) => {
    const valueA = a[key];
    const valueB = b[key];

    if (valueA < valueB) return direction === 'asc' ? -1 : 1;
    if (valueA > valueB) return direction === 'asc' ? 1 : -1;
    return 0;
  });
}

/**
 * Find difference between two arrays
 */
export function difference<T>(array1: T[], array2: T[]): T[] {
  return array1.filter(item => !array2.includes(item));
}

/**
 * Find intersection of two arrays
 */
export function intersection<T>(array1: T[], array2: T[]): T[] {
  return array1.filter(item => array2.includes(item));
}

/**
 * Shuffle an array using Fisher-Yates algorithm
 */
export function shuffle<T>(array: T[]): T[] {
  const result = [...array];
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
}

/**
 * Remove falsy values from an array
 */
export function compact<T>(array: T[]): NonNullable<T>[] {
  return array.filter(Boolean) as NonNullable<T>[];
}

/**
 * Get a random item from an array
 */
export function sample<T>(array: T[]): T | undefined {
  return array[Math.floor(Math.random() * array.length)];
}

/**
 * Get multiple random items from an array
 */
export function sampleSize<T>(array: T[], n: number): T[] {
  return shuffle(array).slice(0, n);
}

/**
 * Move an item in an array from one index to another
 */
export function moveItem<T>(array: T[], fromIndex: number, toIndex: number): T[] {
  const result = [...array];
  const [removed] = result.splice(fromIndex, 1);
  result.splice(toIndex, 0, removed);
  return result;
}

/**
 * Range function to create array of numbers
 */
export function range(
  start: number,
  end: number,
  step: number = 1
): number[] {
  return Array.from(
    { length: Math.floor((end - start) / step) + 1 },
    (_, i) => start + i * step
  );
}

/**
 * Create a counter object from an array
 */
export function countBy<T>(array: T[], key: keyof T): Record<string, number> {
  return array.reduce((acc, item) => {
    const value = String(item[key]);
    acc[value] = (acc[value] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
}