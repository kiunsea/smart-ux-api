export const PI = Math.PI;

export function add(a, b) {
  return a + b;
}

export class Calculator {
  constructor() {
    this.value = 0;
  }
  add(num) {
    this.value += num;
  }
  getValue() {
    return this.value;
  }
}