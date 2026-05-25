import { useMediaQuery } from '@vueuse/core'

const isMobile = useMediaQuery('(max-width: 767px)')
const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 991px)')
const isDesktop = useMediaQuery('(min-width: 992px)')

export function useResponsive() {
  return { isMobile, isTablet, isDesktop }
}
