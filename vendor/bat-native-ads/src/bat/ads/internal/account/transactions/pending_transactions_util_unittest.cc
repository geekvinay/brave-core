/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/account/transactions/pending_transactions_util.h"

#include "bat/ads/internal/account/transactions/transactions_unittest_util.h"
#include "bat/ads/internal/unittest_base.h"
#include "bat/ads/internal/unittest_time_util.h"
#include "bat/ads/internal/unittest_util.h"

// npm run test -- brave_unit_tests --filter=BatAds*

namespace ads {

class BatAdsPendingTransactionsUtilTest : public UnitTestBase {
 protected:
  BatAdsPendingTransactionsUtilTest() = default;

  ~BatAdsPendingTransactionsUtilTest() override = default;
};

TEST_F(BatAdsPendingTransactionsUtilTest, HasPendingTransactionsForThisMonth) {
  // Arrange
  AdvanceClock(TimeFromString("5 November 2020", /* is_local */ true));

  TransactionList transactions;
  const TransactionInfo& transaction =
      BuildTransaction(0.01, ConfirmationType::kViewed);
  transactions.push_back(transaction);

  // Act
  const bool has_pending =
      HasPendingTransactionsForThisMonth(Now(), transactions);

  // Assert
  EXPECT_TRUE(has_pending);
}

TEST_F(BatAdsPendingTransactionsUtilTest,
       DoesNotHavePendingTransactionsForThisMonth) {
  // Arrange
  AdvanceClock(TimeFromString("5 November 2020", /* is_local */ true));

  TransactionList transactions;
  const TransactionInfo& transaction =
      BuildTransaction(0.01, ConfirmationType::kViewed);
  transactions.push_back(transaction);

  AdvanceClock(TimeFromString("25 December 2020", /* is_local */ true));

  // Act
  const bool has_pending =
      HasPendingTransactionsForThisMonth(Now(), transactions);

  // Assert
  EXPECT_FALSE(has_pending);
}

TEST_F(BatAdsPendingTransactionsUtilTest,
       HasPendingTransactionsForPreviousMonth) {
  // Arrange
  AdvanceClock(TimeFromString("5 November 2020", /* is_local */ true));

  TransactionList transactions;
  const TransactionInfo& transaction =
      BuildTransaction(0.01, ConfirmationType::kViewed);
  transactions.push_back(transaction);

  AdvanceClock(TimeFromString("25 December 2020", /* is_local */ true));

  // Act
  const bool has_pending =
      HasPendingTransactionsForPreviousMonth(Now(), transactions);

  // Assert
  EXPECT_TRUE(has_pending);
}

TEST_F(BatAdsPendingTransactionsUtilTest,
       DoesNotHavePendingTransactionsForPreviousMonth) {
  // Arrange
  AdvanceClock(TimeFromString("5 November 2020", /* is_local */ true));

  TransactionList transactions;
  const TransactionInfo& transaction =
      BuildTransaction(0.01, ConfirmationType::kViewed);
  transactions.push_back(transaction);

  // Act
  const bool has_pending =
      HasPendingTransactionsForPreviousMonth(Now(), transactions);

  // Assert
  EXPECT_FALSE(has_pending);
}

}  // namespace ads
