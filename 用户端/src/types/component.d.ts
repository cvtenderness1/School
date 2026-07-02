/**
 * declare module '@vue/runtime-core'
 *   现调整为
 * declare module 'vue'
 */
import wmGuess from '@/components/wmGuess.vue'
import 'vue'
declare module 'vue' {
  export interface GlobalComponents {
    //
    wmGuess: typeof wmGuess
  }
}
export type wmGuessInstance = InstanceType<typeof wmGuess>
