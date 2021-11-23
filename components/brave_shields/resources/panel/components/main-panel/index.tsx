import * as React from 'react'

import * as S from './style'
import Toggle from '../../../../../web-components/toggle'
import { CaratStrongDownIcon } from 'brave-ui/components/icons'
import AdvancedControlsContent from '../advanced-controls-content'
import { getLocale } from '../../../../../common/locale'

function MainPanel () {
  const [isExpanded, setIsExpanded] = React.useState(false)

  return (
    <S.Box>
      <S.PanelHeader>
        <S.SiteTitle>brave.com</S.SiteTitle>
      </S.PanelHeader>
      <S.ToggleBox>
        <Toggle
          brand='shields'
          size='lg'
          accessibleLabel={getLocale('braveShieldsEnable')}
        />
        <S.StatusText>
          {getLocale('braveShields')}
          {' '}
          <span>{getLocale('braveShieldsUp').toUpperCase()}</span>
        </S.StatusText>
        <S.BlockCountBox>
          <S.BlockCount>21</S.BlockCount>
          <S.BlockNote>
            {getLocale('braveShieldsBlockedNote')}
            {' '}
            <a href="#">{getLocale('braveShieldsLearnMore')}</a>
          </S.BlockNote>
        </S.BlockCountBox>
        <S.Footnote>
          {getLocale('braveShieldsBroken')}
        </S.Footnote>
      </S.ToggleBox>
      <S.AdvancedControlsButton
        aria-expanded={isExpanded}
        aria-controls='advanced-controls-content'
        onClick={() => setIsExpanded(x => !x)}
        isOpen={isExpanded}
      >
        {getLocale('braveShieldsAdvancedCtrls')}
        <CaratStrongDownIcon />
      </S.AdvancedControlsButton>
      {isExpanded && <AdvancedControlsContent />}
    </S.Box>
  )
}

export default MainPanel
