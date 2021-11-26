/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/tokens/issuers/issuers_util.h"

#include "bat/ads/internal/tokens/issuers/issuers_info.h"
#include "bat/ads/internal/tokens/issuers/issuers_unittest_util.h"
#include "bat/ads/internal/unittest_base.h"
#include "bat/ads/internal/unittest_util.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

// npm run test -- brave_unit_tests --filter=BatAds*

namespace ads {

class BatAdsIssuersUtilTest : public UnitTestBase {
 protected:
  BatAdsIssuersUtilTest() = default;

  ~BatAdsIssuersUtilTest() override = default;
};

TEST_F(BatAdsIssuersUtilTest, HasIssuersChanged) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const IssuersInfo& issuers =
      BuildIssuers(3600000,
                   {{"Nj2NZ6nJUsK5MJ9ga9tfyctxzpT+GlvENF2TRHU4kBg=", 0.0},
                    {"TFQCiRJocOh0A8+qHQvdu3V/lDpGsZHJOnZzqny6rFg=", 0.0}},
                   {{"PmXS59VTEVIPZckOqGdpjisDidUbhLGbhAhN5tmfhhs=", 0.2},
                    {"Bgk5gT+b96iSr3nD5nuTM/yGQ5klrIe6VC6DDdM6sFs=", 0.1},
                    {"zNWjpwIbghgXvTol3XPLKV3NJoEFtvUoPMiKstiWm3A=", 0.0}});

  const bool has_changed = HasIssuersChanged(issuers);

  // Assert
  EXPECT_TRUE(has_changed);
}

TEST_F(BatAdsIssuersUtilTest, HasIssuersNotChanged) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const IssuersInfo& issuers =
      BuildIssuers(7200000,
                   {{"JsvJluEN35bJBgJWTdW/8dAgPrrTM1I1pXga+o7cllo=", 0.0},
                    {"crDVI1R6xHQZ4D9cQu4muVM5MaaM1QcOT4It8Y/CYlw=", 0.0}},
                   {{"JiwFR2EU/Adf1lgox+xqOVPuc6a/rxdy/LguFG5eaXg=", 0.0},
                    {"XgxwreIbLMu0IIFVk4TKEce6RduNVXngDmU3uixly0M=", 0.1},
                    {"bPE1QE65mkIgytffeu7STOfly+x10BXCGuk5pVlOHQU=", 0.2}});

  const bool has_changed = HasIssuersChanged(issuers);

  // Assert
  EXPECT_FALSE(has_changed);
}

TEST_F(BatAdsIssuersUtilTest, IssuerDoesExistForConfirmationsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = IssuerExistsForType(IssuerType::kConfirmations);

  // Assert
  EXPECT_TRUE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, IssuerDoesNotExistForConfirmationsType) {
  // Arrange
  const IssuersInfo& issuers =
      BuildIssuers(7200000, {},
                   {{"JiwFR2EU/Adf1lgox+xqOVPuc6a/rxdy/LguFG5eaXg=", 0.0},
                    {"XgxwreIbLMu0IIFVk4TKEce6RduNVXngDmU3uixly0M=", 0.1},
                    {"bPE1QE65mkIgytffeu7STOfly+x10BXCGuk5pVlOHQU=", 0.2}});

  SetIssuers(issuers);

  // Act
  const bool does_exist = IssuerExistsForType(IssuerType::kConfirmations);

  // Assert
  EXPECT_FALSE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, IssuerDoesExistForPaymentsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = IssuerExistsForType(IssuerType::kPayments);

  // Assert
  EXPECT_TRUE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, IssuerDoesNotExistForPaymentsType) {
  // Arrange
  const IssuersInfo& issuers =
      BuildIssuers(7200000,
                   {{"JsvJluEN35bJBgJWTdW/8dAgPrrTM1I1pXga+o7cllo=", 0.0},
                    {"cKo0rk1iS8Obgyni0X3RRoydDIGHsivTkfX/TM1Xl24=", 0.0}},
                   {});

  SetIssuers(issuers);

  // Act
  const bool does_exist = IssuerExistsForType(IssuerType::kPayments);

  // Assert
  EXPECT_FALSE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, PublickKeyDoesExistForConfirmationsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = PublicKeyExistsForIssuerType(
      IssuerType::kConfirmations,
      "crDVI1R6xHQZ4D9cQu4muVM5MaaM1QcOT4It8Y/CYlw=");

  // Assert
  EXPECT_TRUE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, PublickKeyDoesNotExistForConfirmationsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = PublicKeyExistsForIssuerType(
      IssuerType::kConfirmations,
      "Nj2NZ6nJUsK5MJ9ga9tfyctxzpT+GlvENF2TRHU4kBg=");

  // Assert
  EXPECT_FALSE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, PublickKeyDoesExistForPaymentsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = PublicKeyExistsForIssuerType(
      IssuerType::kPayments, "bPE1QE65mkIgytffeu7STOfly+x10BXCGuk5pVlOHQU=");

  // Assert
  EXPECT_TRUE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, PublickKeyDoesNotExistForPaymentsType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const bool does_exist = PublicKeyExistsForIssuerType(
      IssuerType::kPayments, "zNWjpwIbghgXvTol3XPLKV3NJoEFtvUoPMiKstiWm3A=");

  // Assert
  EXPECT_FALSE(does_exist);
}

TEST_F(BatAdsIssuersUtilTest, GetSmallestDenominationForIssuerType) {
  // Arrange
  BuildAndSetIssuers();

  // Act
  const absl::optional<double> smallest_denomination_optional =
      GetSmallestDenominationForIssuerType(IssuerType::kPayments);
  ASSERT_TRUE(smallest_denomination_optional);

  const double smallest_denomination = smallest_denomination_optional.value();

  // Assert
  EXPECT_EQ(0.1, smallest_denomination);
}

TEST_F(BatAdsIssuersUtilTest, GetSmallestDenominationForMissingIssuerType) {
  // Arrange

  // Act
  const absl::optional<double> smallest_denomination_optional =
      GetSmallestDenominationForIssuerType(IssuerType::kPayments);

  // Assert
  EXPECT_FALSE(smallest_denomination_optional);
}

}  // namespace ads
