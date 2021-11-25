import { EthereumSignedTx } from 'trezor-connect/lib/typescript'
import { HardwareVendor, SupportedCoins } from '../api/getKeyringsByType'

export type HardwareWalletErrorType =
  | 'deviceNotConnected'
  | 'openEthereumApp'
  | 'deviceBusy'

export interface SignHardwareTransactionType {
  success: boolean
  error?: string
  deviceError?: HardwareWalletErrorType
}
export interface SignatureVRS {
  v: number
  r: string
  s: string
}

export type HardwareOperationResult = {
  success: boolean
  error?: string
  code?: string | number
}

export type SignHardwareTransactionOperationResult = HardwareOperationResult & {
  payload?: EthereumSignedTx
}

export type SignHardwareMessageOperationResult = HardwareOperationResult & {
  payload?: string
}

export interface TrezorBridgeAccountsPayload {
  success: boolean
  accounts: HardwareWalletAccount[]
  error?: string
}

// Keep in sync with components/brave_wallet/common/brave_wallet.mojom until
// we auto generate this type file from mojo.
export interface HardwareWalletAccount {
  address: string
  derivationPath: string
  name: string
  hardwareVendor: HardwareVendor
  deviceId: string
  coin: SupportedCoins
}

export enum LedgerDerivationPaths {
  LedgerLive = 'ledger-live',
  Legacy = 'legacy'
}

export enum TrezorDerivationPaths {
  Default = 'trezor'
}

const DerivationSchemeTypes = [LedgerDerivationPaths.LedgerLive, LedgerDerivationPaths.Legacy, TrezorDerivationPaths.Default] as const
export type HardwareDerivationScheme = typeof DerivationSchemeTypes[number]
