import * as React from 'react'

import * as S from './style'
import { CaratStrongDownIcon } from 'brave-ui/components/icons'
import Toggle from '../../../../../web-components/toggle'
import { getLocale } from '../../../../../common/locale'

function GlobalSettings () {
  return (
    <S.SettingsBox>
      <S.SettingsTitle>{getLocale('braveShieldsGlobalSettingsTitle')}</S.SettingsTitle>
      <a href='brave://settings/shields' target='_blank'>
        <S.GlobeIcon />
        {getLocale('braveShieldsChangeDefaults')}
      </a>
      <a href='brave://adblock' target='_blank'>
        <S.ListIcon />
        {getLocale('braveShieldsCustomizeAdblockLists')}
      </a>
    </S.SettingsBox>
  )
}

function AdvancedControlsContent () {
  const trackersOptions = [
    { text: getLocale('braveShieldsTrackersAndAdsBlockedStd') },
    { text: getLocale('braveShieldsTrackersAndAdsBlockedAgg') },
    { text: getLocale('braveShieldsTrackersAndAdsAllowAll') }
  ]

  const cookiesOptions = [
    { text: getLocale('braveShieldsCrossCookiesBlocked') },
    { text: getLocale('braveShieldsCookiesBlocked') },
    { text: getLocale('braveShieldsCookiesBlockedAll') }
  ]

  const fingerprintingOptions = [
    { text: getLocale('braveShieldsFingerprintingBlockedStd') },
    { text: getLocale('braveShieldsFingerprintingBlockedAgg') },
    { text: getLocale('braveShieldsFingerprintingAllowAll') }
  ]

  return (
    <section
      id='advanced-controls-content'
      aria-label={getLocale('braveShieldsAdvancedCtrls')}
    >
      <S.SettingsBox>
        <S.SettingsTitle>brave.com</S.SettingsTitle>
        <S.SettingsDesc>{getLocale('braveShieldSettingsDescription')}</S.SettingsDesc>
        <S.ControlGroup>
          <S.ControlCount
            aria-label={getLocale('braveShieldsTrackersAndAds')}
            aria-expanded='false'
          >
            <CaratStrongDownIcon />
            <span>10</span>
          </S.ControlCount>
          <select aria-label={getLocale('braveShieldsTrackersAndAds')}>
              {trackersOptions.map((entry, i) => {
                return (
                  <option key={i}>
                    {entry.text}
                  </option>
                )
              })}
          </select>
        </S.ControlGroup>
        <S.ControlGroup>
          <S.ControlCount
            aria-label={getLocale('braveShieldsConnectionsUpgraded')}
            aria-expanded='false'
            disabled
          >
            <CaratStrongDownIcon />
            <span>0</span>
          </S.ControlCount>
          <label>
            <span>{getLocale('braveShieldsConnectionsUpgraded')}</span>
            <Toggle
              size='sm'
              accessibleLabel='Enable HTTPS'
            />
          </label>
        </S.ControlGroup>
        <S.ControlGroup>
          <S.ControlCount
            aria-label={getLocale('braveShieldsScriptsBlocked')}
            aria-expanded='false'
            disabled
          >
            <CaratStrongDownIcon />
            <span>0</span>
          </S.ControlCount>
          <label>
            <span>{getLocale('braveShieldsScriptsBlocked')}</span>
            <Toggle
              size='sm'
              accessibleLabel={getLocale('braveShieldsScriptsBlockedEnable')}
            />
          </label>
        </S.ControlGroup>
        <S.ControlGroup>
          <S.ControlCount disabled />
          <select aria-label={getLocale('braveShieldsCookiesBlocked')}>
              {cookiesOptions.map((entry, i) => {
                return (
                  <option key={i}>
                    {entry.text}
                  </option>
                )
              })}
          </select>
        </S.ControlGroup>
        <S.ControlGroup>
          <S.ControlCount
            aria-label={getLocale('braveShieldsFingerprintingBlocked')}
            aria-expanded='false'
            disabled
          >
            <CaratStrongDownIcon />
            <span>0</span>
          </S.ControlCount>
          <select aria-label={getLocale('braveShieldsFingerprintingBlocked')}>
            {fingerprintingOptions.map((entry, i) => {
                return (
                  <option key={i}>
                    {entry.text}
                  </option>
                )
              })}
          </select>
        </S.ControlGroup>
      </S.SettingsBox>
      <GlobalSettings />
  </section>
  )
}

export default AdvancedControlsContent
