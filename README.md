English | [简体中文](./README_ZH.md)

# Chat
## Product Introduction
You only need to integrate Chat SDK to easily gain chat, conversation, group capabilities, and you can also communicate with other products such as whiteboards through signaling messages. Chat can cover various business scenarios, support the access and use of various platforms, and fully meet the communication needs.

<table style="text-align:center; vertical-align:middle; width:200px">
  <tr>
    <th style="text-align:center;" width="200px">Android Experience App</th>
  </tr>
  <tr>
    <td><img style="width:200px" src="https://qcloudimg.tencent-cloud.cn/raw/078fbb462abd2253e4732487cad8a66d.png"/></td>
   </tr>
</table>

TUIKit is a UI component library based on Chat SDK. It provides universal UI components to offer features such as conversation, chat, search, relationship chain, group, and audio/video call features.

<img src="https://qcloudimg.tencent-cloud.cn/raw/9c893f1a9c6368c82d44586907d5293d.png" width="800"/>

Explore more docs about [TUIKit Library Overview](https://trtc.io/document/50062?platform=android&product=chat&menulabel=uikit).

## Run through Demo
This document introduces how to quickly run through the Chat demo on the Android platform.
For the other platforms, please refer to document：

- [**chat-uikit-ios**](https://github.com/TencentCloud/chat-uikit-ios)

- [**chat-uikit-flutter**](https://github.com/TencentCloud/chat-uikit-flutter)

- [**chat-uikit-vue**](https://github.com/TencentCloud/chat-uikit-vue)

- [**chat-uikit-react**](https://github.com/TencentCloud/chat-uikit-react)

- [**chat-uikit-uniapp**](https://github.com/TencentCloud/chat-uikit-uniapp)

- [**chat-uikit-wechat**](https://github.com/TencentCloud/chat-uikit-wechat)

In respect for the copyright of the emoji design, the Chat Demo/TUIKit project does not include the cutouts of large emoji elements. Please replace them with your own designed or copyrighted emoji packs before the official launch for commercial use. The default small yellow face emoji pack is copyrighted by Tencent Cloud and can be authorized for a fee. If you wish to obtain authorization, please [Contact us](https://trtc.io/contact).

<img src="https://qcloudimg.tencent-cloud.cn/image/document/6438e8feb7bba909511e0d798dfaf91d.png" width="300px" />

### Step 1. Create an App
1. Log in to the [Chat Console](https://console.trtc.io/).
 >If you already have an app, record its SDKAppID and go to **step 2**.
 >
2. On the **Application List** page, click **Create Application**.
3. In the **Create Application** dialog box, enter the app information and click **Confirm**.
 After the app is created, an app ID (SDKAppID) will be automatically generated, which should be noted down.

### Step 2: Obtain Key Information

1. Click **Application Configuration** in the row of the target app to enter the app details page.
2. Click **View Key** and copy and save the key information.
 > Please store the key information properly to prevent leakage.

### Step 3: Download and Configure the Demo Source Code

1. Clone this Chat demo project.
2. Open the project in the terminal directory and find the `GenerateTestUserSig` file in the following paths:
 <table>
     <tr>
         <th nowrap="nowrap">Platform</th>  
         <th nowrap="nowrap">Relative Path to File</th>  
     </tr>
  <tr>      
      <td>Android</td>   
      <td>Android/Demo/app/src/main/java/com/tencent/qcloud/tim/demo/signature/GenerateTestUserSig.java</td>   
     </tr> 
  <tr>
      <td>iOS</td>   
      <td>iOS/Demo/TUIKitDemo/Private/GenerateTestUserSig.h</td>
     </tr> 
</table>


 >In this document, an Android project is opened by using Android Studio as an example.
  >
3. Set relevant parameters in the `GenerateTestUserSig` file:
 - SDKAPPID: set it to the SDKAppID obtained in **step 1**.
 - SECRETKEY: enter the actual key information obtained in **step 2**.

<img src="https://cloudcache.intl.tencent-cloud.com/cms/backend-cms/e0d841971cd711ef9b9e5254002977b6.png" width="800"/>


> In this document, the method to obtain UserSig is to configure a SECRETKEY in the client code. In this method, the SECRETKEY is vulnerable to decompilation and reverse engineering. Once your SECRETKEY is leaked, attackers can steal your Tencent Cloud traffic. Therefore, **this method is only suitable for locally running a demo project and feature debugging**.
>The correct `UserSig` distribution method is to integrate the calculation code of `UserSig` into your server and provide an application-oriented API. When `UserSig` is needed, your app can send a request to the business server for a dynamic `UserSig`. For more information, please see [How do I calculate UserSig on the server?](https://trtc.io/document/34385?product=chat&menulabel=serverapis).

### Step 4: Compile and Run the Demo (All Features)
Import the demo project with Android Studio, and then compile and run it.

> **The demo integrates the audio and video call function by default. Since the audio and video SDK that this function depends on does not support the emulator, please use a real device to debug or run the demo.**
> 
### Step 5: Compile and Run the Demo (Removing the Audio/Video Call Feature)
If you do not need the audio/video call feature, you only delete the audio/video call integration code as shown in the figure below from the `build.gradle` file under the `app` module:

![](https://qcloudimg.tencent-cloud.cn/raw/4ad9df5f0b3d1068427a51937613da92.jpg)

```groovy
api project(':tuicallkit')
```
After the preceding steps are completed, the audio and video call entries in the demo are hidden.
The conversation UIs before and after TUICallKit masking are as follows:

| before | After |
|---------|---------|
| <img width="240" alt="GitHub_ChatIncludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878435-75c56857-a8c5-4262-b0cf-71a7d773b50c.png">  | <img width="240" alt="GitHub_ChatExcludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878234-ce5c4dd3-e6aa-4352-9c13-dababfd15c48.png"> |

The contact profile UIs before and after TUICallKit masking are as follows:

| before | After |
|---------|---------|
| <img width="240" alt="GitHub_ContactIncludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878892-218cb7a3-977a-4277-bda3-903360600742.png"> | <img width="240" alt="GitHub_ContactExcludeCallMinimalist" src="https://user-images.githubusercontent.com/85340225/205878978-106e0230-5111-485a-8c65-a9662e2dda9d.png"> |

> The above only shows how to remove the audio/video call feature from the demo. Developers can customize the demo according to their business requirements.

### Step 6: Compile and Run the Demo (Removing the Search Module)
If you do not need the search feature, you only delete the line of code as shown in the figure below from the `build.gradle` file under the `app` module:

![](https://qcloudimg.tencent-cloud.cn/raw/7e2685017b93e418dadd1599bcb0a3b6.jpg)

```groovy
api project(':tuisearch')
```
After the preceding steps are completed, the message search box in the demo is hidden.

The message UIs before and after TUISearch masking are as follows:
 
| before | After |
|---------|---------|
| <img width="240" alt="GitHub_ConversationIncludeSearchMinimalist" src="https://user-images.githubusercontent.com/85340225/205879099-1577d68a-a6c2-4413-8ebe-6742f5e4aa7c.png"> | <img width="240" alt="GitHub_ConversationExcludeSearchMinimalist" src="https://user-images.githubusercontent.com/85340225/205879135-0d5753b9-029f-4c68-9dfa-9c587277106c.png"> |

> The above only shows how to remove the search feature from the demo. Developers can customize the demo according to their business requirements.
