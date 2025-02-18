import { 
  trigger, 
  state, 
  style, 
  transition, 
  animate 
} from '@angular/animations';

export const fadeInOut = trigger('fadeInOut', [
  state('void', style({
    opacity: 0,
    transform: 'translateY(-20px)'
  })),
  transition('void => *', [
    animate('300ms ease-out')
  ]),
  transition('* => void', [
    animate('300ms ease-in')
  ])
]);