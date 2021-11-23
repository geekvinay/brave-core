// Copyright (c) 2021 The Brave Authors. All rights reserved.
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this file,
// you can obtain one at http://mozilla.org/MPL/2.0/.

#ifndef BRAVE_BROWSER_UI_WEBUI_BRAVE_SHIELDS_SHIELDS_PANEL_HANDLER_H_
#define BRAVE_BROWSER_UI_WEBUI_BRAVE_SHIELDS_SHIELDS_PANEL_HANDLER_H_

#include <string>
#include <vector>

#include "brave/components/brave_shields/common/brave_shields_panel.mojom.h"
#include "mojo/public/cpp/bindings/pending_receiver.h"
#include "mojo/public/cpp/bindings/receiver.h"
#include "ui/webui/mojo_bubble_web_ui_controller.h"

namespace content {
class WebUI;
}  // namespace content

class ShieldsPanelHandler : public brave_shields_panel::mojom::PanelHandler {
 public:
  using GetWebContentsForTabCallback =
      base::RepeatingCallback<content::WebContents*(int32_t)>;

  ShieldsPanelHandler(
      mojo::PendingReceiver<brave_shields_panel::mojom::PanelHandler> receiver,
      ui::MojoBubbleWebUIController* webui_controller);

  ShieldsPanelHandler(const ShieldsPanelHandler&) = delete;
  ShieldsPanelHandler& operator=(const ShieldsPanelHandler&) = delete;
  ~ShieldsPanelHandler() override;

  // brave_shields_panel::mojom::PanelHandler:
  void ShowUI() override;
  void CloseUI() override;

 private:
  mojo::Receiver<brave_shields_panel::mojom::PanelHandler> receiver_;
  ui::MojoBubbleWebUIController* const webui_controller_;
};

#endif  // BRAVE_BROWSER_UI_WEBUI_BRAVE_SHIELDS_SHIELDS_PANEL_HANDLER_H_
