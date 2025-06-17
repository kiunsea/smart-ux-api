import { PI, add, Calculator } from './myModule.js';

console.log(PI); // Output: 3.141592653589793
console.log(add(5, 3)); // Output: 8

const calc = new Calculator();
calc.add(10);
console.log(calc.getValue()); // Output: 10