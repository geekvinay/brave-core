// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#include "brave/browser/ui/webui/brave_shields/shields_panel_handler.h"

#include <utility>

ShieldsPanelHandler::ShieldsPanelHandler(
    mojo::PendingReceiver<brave_shields_panel::mojom::PanelHandler> receiver,
    ui::MojoBubbleWebUIController* webui_controller)
    : receiver_(this, std::move(receiver)),
      webui_controller_(webui_controller) {}

ShieldsPanelHandler::~ShieldsPanelHandler() = default;

void ShieldsPanelHandler::ShowUI() {
  auto embedder = webui_controller_->embedder();
  if (embedder) {
    embedder->ShowUI();
  }
}

void ShieldsPanelHandler::CloseUI() {
  auto embedder = webui_controller_->embedder();
  if (embedder) {
    embedder->CloseUI();
  }
}
