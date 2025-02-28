import SwiftUI
import shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate
    
    init() {
        Logger_nativeKt.installLogger()
    }

    var body: some Scene {
        WindowGroup {
            RootView(root: appDelegate.root)
                .ignoresSafeArea(.all)
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    let root: RootComponent = RootComponent(
        componentContext: DefaultComponentContext(lifecycle: ApplicationLifecycle())
    )
}
