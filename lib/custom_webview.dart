import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

const String _viewType = 'FlutterCustomWebView';

enum CallMethod { post, get }

class CustomWebView extends StatelessWidget {
  const CustomWebView({
    super.key,
    this.header,
    this.body,
    this.callMethod = CallMethod.get,
    required this.url,
  });

  final Map<String, String>? header;
  final Map<String, String>? body;
  final String url;
  final CallMethod callMethod;

  @override
  Widget build(BuildContext context) {
    return PlatformViewLink(
      viewType: _viewType,
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: _viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: {
            "header": header ?? {},
            "body": body ?? {},
            "url": url,
            "method": callMethod == CallMethod.post ? "post" : "get",
          },
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}
