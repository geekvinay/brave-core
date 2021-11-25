/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

import { assert } from 'chrome://resources/js/assert.m.js'
import { TREZOR_HARDWARE_VENDOR, LEDGER_HARDWARE_VENDOR, KeyringControllerRemote } from 'gen/brave/components/brave_wallet/common/brave_wallet.mojom.m.js'
import LedgerBridgeKeyring from '../../common/hardware/ledgerjs/eth_ledger_bridge_keyring'
import TrezorBridgeKeyring from '../../common/hardware/trezor/trezor_bridge_keyring'
import { LedgerKeyring } from '../hardware/hardwareKeyring'
import FileCoinLedgerKeyring from '../hardware/ledgerjs/filecoin_ledger_keyring'
export type HardwareKeyring = LedgerBridgeKeyring | TrezorBridgeKeyring

export enum SupportedCoins {
  ETH = 'e',
  FILECOIN = 'f'
}

const VendorTypes = [TREZOR_HARDWARE_VENDOR, LEDGER_HARDWARE_VENDOR] as const
export type HardwareVendor = typeof VendorTypes[number]

// Lazy instances for keyrings
let ethereumHardwareKeyring: LedgerBridgeKeyring
let filecoinHardwareKeyring: FileCoinLedgerKeyring
let trezorHardwareKeyring: TrezorBridgeKeyring
let keyringController: KeyringControllerRemote

export function getBraveKeyring (): KeyringControllerRemote {
  if (!keyringController) {
    /** @type {!braveWallet.mojom.KeyringControllerRemote} */
    keyringController = new KeyringControllerRemote()
  }
  return keyringController
}

export function getHardwareKeyring (type: HardwareVendor, coin: SupportedCoins = SupportedCoins.ETH): LedgerKeyring | TrezorBridgeKeyring {
  if (type === LEDGER_HARDWARE_VENDOR) {
    const ledgerKeyring = getLedgerHardwareKeyring(coin)
    assert(type === ledgerKeyring.type())
    return ledgerKeyring
  }

  const trezorKeyring = getTrezorHardwareKeyring()
  assert(type === trezorHardwareKeyring.type())
  return trezorKeyring
}

export function getLedgerHardwareKeyring (coin: SupportedCoins): LedgerKeyring {
  if (coin === SupportedCoins.ETH) {
    if (!ethereumHardwareKeyring) {
      ethereumHardwareKeyring = new LedgerBridgeKeyring()
    }
    return ethereumHardwareKeyring
  }
  assert(coin === SupportedCoins.FILECOIN)
  if (!filecoinHardwareKeyring) {
    filecoinHardwareKeyring = new FileCoinLedgerKeyring()
  }
  console.log(filecoinHardwareKeyring)
  return filecoinHardwareKeyring
}

export function getTrezorHardwareKeyring (): TrezorBridgeKeyring {
  if (!trezorHardwareKeyring) {
    trezorHardwareKeyring = new TrezorBridgeKeyring()
  }
  return trezorHardwareKeyring
}
