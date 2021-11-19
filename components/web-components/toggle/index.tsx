import * as React from 'react'

import * as S from './style'

export type Size = 'sm' | 'lg'
export type Brand = 'vpn' | 'shields'
interface Props {
  size?: Size
  brand?: Brand
  isOn?: boolean
  disabled?: boolean
  accessibleLabel?: string
  onChange?: (isOn?: boolean) => unknown
}

function Toggle (props: Props) {
  const [isOn, setIsOn] = React.useState(props.isOn ?? false)

  const onToggleClick = () => {
    const activated = !isOn
    setIsOn(activated)
    props.onChange?.(activated)
  }

  return (
    <S.ToggleBox
      type='button'
      role='switch'
      aria-checked={isOn}
      aria-label={props.accessibleLabel}
      onClick={onToggleClick}
      isActive={isOn}
      disabled={props.disabled}
      size={props.size}
      brand={props.brand}
    >
      <S.Knob />
    </S.ToggleBox>
  )
}

export default Toggle
