/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#ifndef BRAVE_COMPONENTS_BRAVE_WALLET_BROWSER_ETH_PENDING_TX_TRACKER_H_
#define BRAVE_COMPONENTS_BRAVE_WALLET_BROWSER_ETH_PENDING_TX_TRACKER_H_

#include <string>

#include "base/containers/flat_map.h"
#include "base/gtest_prod_util.h"
#include "base/memory/weak_ptr.h"
#include "brave/components/brave_wallet/browser/eth_tx_state_manager.h"
#include "brave/components/brave_wallet/common/brave_wallet_types.h"

namespace brave_wallet {

class EthNonceTracker;
class EthJsonRpcController;

class EthPendingTxTracker {
 public:
  EthPendingTxTracker(EthTxStateManager* tx_state_manager,
                      EthJsonRpcController* rpc_controller,
                      EthNonceTracker* nonce_tracker);
  ~EthPendingTxTracker();
  EthPendingTxTracker(const EthPendingTxTracker&) = delete;
  EthPendingTxTracker operator=(const EthPendingTxTracker&) = delete;

  bool UpdatePendingTransactions(size_t* num_pending);
  void ResubmitPendingTransactions();

 private:
  FRIEND_TEST_ALL_PREFIXES(EthPendingTxTrackerUnitTest, IsNonceTaken);
  FRIEND_TEST_ALL_PREFIXES(EthPendingTxTrackerUnitTest, ShouldTxDropped);
  FRIEND_TEST_ALL_PREFIXES(EthPendingTxTrackerUnitTest, DropTransaction);

  void OnGetTxReceipt(std::string id, bool status, TransactionReceipt receipt);
  void OnGetNetworkNonce(std::string address, bool status, uint256_t result);
  void OnSendRawTransaction(bool status, const std::string& tx_hash);

  bool IsNonceTaken(const EthTxStateManager::TxMeta&);
  bool ShouldTxDropped(const EthTxStateManager::TxMeta&);

  void DropTransaction(EthTxStateManager::TxMeta*);

  // (address, nonce)
  base::flat_map<std::string, uint256_t> network_nonce_map_;
  // (txHash, count)
  base::flat_map<std::string, uint8_t> dropped_blocks_counter_;

  EthTxStateManager* tx_state_manager_;
  EthJsonRpcController* rpc_controller_;
  EthNonceTracker* nonce_tracker_;

  base::WeakPtrFactory<EthPendingTxTracker> weak_factory_;
};

}  // namespace brave_wallet

#endif  // BRAVE_COMPONENTS_BRAVE_WALLET_BROWSER_ETH_PENDING_TX_TRACKER_H_
