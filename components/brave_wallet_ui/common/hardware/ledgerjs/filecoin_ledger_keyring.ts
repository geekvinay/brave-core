/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

import { LEDGER_HARDWARE_VENDOR } from 'gen/brave/components/brave_wallet/common/brave_wallet.mojom.m.js'
import {
  LedgerProvider, TransportWrapper
} from '@glif/filecoin-wallet-provider'
import Transport from '@ledgerhq/hw-transport';
import { CoinType } from '@glif/filecoin-address';
import { LedgerKeyring } from '../hardwareKeyring';
import { HardwareVendor } from '../../api/getKeyringsByType';
import { GetAccountsHardwareOperationResult, HardwareOperationResult, SignHardwareMessageOperationResult, SignHardwareTransactionOperationResult } from '../../hardware_operations';
import { hardwareDeviceIdFromAddress } from '../hardwareDeviceIdFromAddress';

export default class FileCoinLedgerKeyring extends LedgerKeyring {
  constructor () {
    super()
  }
  private coinType: CoinType = CoinType.MAIN
  private deviceId: string
  
  coin = () => {
    return this.coinType
  }

  private provider?: LedgerProvider

  type = (): HardwareVendor => {
    return LEDGER_HARDWARE_VENDOR
  }

  getAccounts = async (from: number, to: number, scheme: string): Promise<GetAccountsHardwareOperationResult> => {
    console.log(await this.provider?.getAccounts(0, 1, CoinType.MAIN))
    return { success: false }
  }
  isUnlocked = () => {
    return this.provider !== undefined
  }

  makeApp = async (transport: Transport) => {
    try {
      this.provider = new LedgerProvider({
        transport: transport,
        minLedgerVersion: {
          major: 1,
          minor: 0,
          patch: 0
        }
      })

      await this.provider.ready()
    } catch(e) {
      console.log(e)
    }
  }

  unlock = async (): Promise<HardwareOperationResult> => {
    if (this.provider) {
      return { success: true }
    }
    const transportWrapper = new TransportWrapper()
    await this.makeApp(transportWrapper.transport)
    if (!this.provider) {
      return { success: false }
    }

    transportWrapper.transport.on('disconnect', this.onDisconnected)
    const app: LedgerProvider = this.provider
    const address = app.getAccounts(0, 1, this.coin() as CoinType)
    console.log(address)
    this.deviceId = await hardwareDeviceIdFromAddress(address[0])
    console.log(this.deviceId)
    return { success: this.isUnlocked() }

  }
  signPersonalMessage(path: string, address: string, message: string): Promise<SignHardwareMessageOperationResult> {
    throw new Error('Method not implemented.');
  }
  signTransaction(path: string, rawTxHex: string): Promise<SignHardwareTransactionOperationResult> {
    throw new Error('Method not implemented.');
  }

  private onDisconnected = (e: any) => {
    if (e.name !== 'DisconnectedDevice') {
      return
    }
    this.provider = undefined
  }
}