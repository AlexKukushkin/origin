   M S B A M L   `   `   `  ����D  @DB Worker, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null3clr-namespace:SnowboardScreensSnowboardScreens  5      v rMicrosoft.Expression.Prototyping.SketchControls, Version=4.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35�vclr-namespace:Microsoft.Expression.Prototyping.SketchControls;assembly=Microsoft.Expression.Prototyping.SketchControls/Microsoft.Expression.Prototyping.SketchControls 5      c _System.Windows.Interactivity, Version=4.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35qPclr-namespace:System.Windows.Interactivity;assembly=System.Windows.InteractivitySystem.Windows.Interactivity 5	      h dMicrosoft.Expression.Interactions, Version=4.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35�`clr-namespace:Microsoft.Expression.Interactivity.Core;assembly=Microsoft.Expression.Interactions'Microsoft.Expression.Interactivity.Core 5
      �aclr-namespace:Microsoft.Expression.Interactivity.Media;assembly=Microsoft.Expression.Interactions(Microsoft.Expression.Interactivity.Media 5      �aclr-namespace:Microsoft.Expression.Interactivity.Input;assembly=Microsoft.Expression.Interactions(Microsoft.Expression.Interactivity.Input 5      �bclr-namespace:Microsoft.Expression.Interactivity.Layout;assembly=Microsoft.Expression.Interactions)Microsoft.Expression.Interactivity.Layout 5      u qMicrosoft.Expression.Prototyping.Interactivity, Version=4.5.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35�oclr-namespace:Microsoft.Expression.Prototyping.Behavior;assembly=Microsoft.Expression.Prototyping.Interactivity)Microsoft.Expression.Prototyping.Behavior 5          SnowboardScreens.Bindings   5      R NWindowsBase, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35W SPresentationCore, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35\ XPresentationFramework, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35D 9http://schemas.microsoft.com/winfx/2006/xaml/presentation    R NSystem.Xaml, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     5      @d2http://schemas.microsoft.com/expression/blend/2008     5      Jmc;http://schemas.openxmlformats.org/markup-compatibility/2006     5      0localclr-namespace:SnowboardScreens     5      �psvclr-namespace:Microsoft.Expression.Prototyping.SketchControls;assembly=Microsoft.Expression.Prototyping.SketchControls     5      ^iPclr-namespace:System.Windows.Interactivity;assembly=System.Windows.Interactivity     5	      oic`clr-namespace:Microsoft.Expression.Interactivity.Core;assembly=Microsoft.Expression.Interactions     5
      pimaclr-namespace:Microsoft.Expression.Interactivity.Media;assembly=Microsoft.Expression.Interactions     5      piiaclr-namespace:Microsoft.Expression.Interactivity.Input;assembly=Microsoft.Expression.Interactions     5      qilbclr-namespace:Microsoft.Expression.Interactivity.Layout;assembly=Microsoft.Expression.Interactions     5      ~pboclr-namespace:Microsoft.Expression.Prototyping.Behavior;assembly=Microsoft.Expression.Prototyping.Interactivity     5      ZAnnoIhttp://schemas.microsoft.com/expression/blend/extensions/annotations/2008     5      -   5          >�Name$  Bindings_Name��5      $	��640��6   $	��480��6%   .��5      � -   5         �Name$ 
LayoutRoot��5      ���	#FFFFFFFF'  !System.Windows.VisualStateManager   VisualStateGroups 5      %  System.Windows.VisualStateGroup  5      -   5        
Popups��   Transitions 5      %  System.Windows.VisualTransition  5         GeneratedDuration$ 00:00:01D�6     5         From$
 Cart��6   $ 00:00:00.2000000D�6$   	   To$ NoPopup��6I      
Storyboard. 5      �� .��5   	   U�  g� 	BeginTime$ 00:00:00X�6'   	 �� 
TargetName$	 sketchRectangleUC��6<   
 �� TargetProperty$
 (UIElement.Opacity)�6f   .\�5   
   ��  O� KeyTime$ 00:00:00.2000000��6    O� Value$ 0Q�6:   5    
   5!   	   5"        5#      $
 Chat��6   $ 00:00:00.2000000D�6$   $ NoPopup��6I   . 5$      �� .��5%   	   U� $ 00:00:00X�6'   $	 sketchRectangleUC1��6<   $
 (UIElement.Opacity)�6g   .\�5&   
   �� $ 00:00:00.2000000��6   $ 0Q�6:   5'   
   5(   	   5)         States. 5+         System.Windows.VisualState  -   5        NoPopup��5+        5,      -   5        Cart��   
Storyboard. 5-      �� .��5.      U� $ 00:00:00X�6&    g� Duration$ 00:00:00.0010000D�6;   $	 
Cart_Popup��6W   $
 (UIElement.Opacity)�6z   .\�5/   	   �� $ 00:00:00��6   $ 1Q�61   50   	   �� 51      $ 00:00:00X�6%   $ 00:00:00.0010000D�6:   $	 
Cart_Popup��6V   $0
 *(Panel.Background).(SolidColorBrush.Color)�6y   .g�52   	   ��  �� KeyTime$ 00:00:00��6    �� Value$ 	#FFDBFFB1��60   53   	   U� 54      $ 00:00:00X�6&   $ 00:00:00.0010000D�6;   $	 textBox��6W   $
 (UIElement.Opacity)�6w   .\�55   	   �� $ 00:00:00��6   $ 1Q�61   56   	   57      58        59      -   5        Chat��. 5:      �� .��5;      U� $ 00:00:00X�6&   $ 00:00:00.0010000D�6;   $	 
Chat_Popup��6W   $
 (UIElement.Opacity)�6z   .\�5<   	   �� $ 00:00:00��6   $ 1Q�61   5=   	   �� 5>      $ 00:00:00X�6%   $ 00:00:00.0010000D�6:   $	 
Chat_Popup��6V   $0
 *(Panel.Background).(SolidColorBrush.Color)�6y   .g�5?   	   �� $ 00:00:00��6   $ 	#FFACC1CE��60   5@   	   U� 5A      $ 00:00:00X�6&   $ 00:00:00.0010000D�6;   $	 textBox1��6W   $
 (UIElement.Opacity)�6x   .\�5B   	   �� $ 00:00:00��6   $ 1Q�61   5C   	   5D      5E      5F      .+�5H      G  AMicrosoft.Expression.Prototyping.SketchControls.SketchRectangleUC  $��
8,112,80,8q�6      Rectangle-Sketch#���   6-   ��6X   Z� �� 5I      $��Right=�6   $��	0,112,8,0q�6'     Button-Sketch#���  6:   $	��Top=�6b   $��68��6z   $��53��6�   $��	View
cart��6�   .  (System.Windows.Interactivity.Interaction   Triggers 5J      /  )System.Windows.Interactivity.EventTrigger  5K         	EventName$ Click��6   .  (System.Windows.Interactivity.TriggerBase   Actions. 5L      C	  =Microsoft.Expression.Prototyping.Behavior.ActivateStateAction	   	  TargetScreen$ SnowboardScreens.Bindings��6    	  TargetState$
 Cart��6G   5M      5O      �� 5P      $��Right=�6   $��0,0,8,8q�6'   #���  68   $��Bottom=�6`   $��68��6{   $��51��6�   $��
Live 
chat��6�    5Q        5R      $ Click��6   . 5S      	  $ SnowboardScreens.Bindings��6   $
 Chat��6G   5T      5V      � 5W      -   5        $ 
Cart_Popup��5W   	   $��0,126,93,98q�6    J� Opacity$ 0Q�62   ���	#FF000000$��Right=�6U   $	��213��6q    5X        5Y      $ 	MouseDown��6   . 5Z      	  $ SnowboardScreens.Bindings��6   $ NoPopup��6G   5[      .+�5]        -   5          Name$ sketchRectangleUC��5]      #���   65   ��6`   Z� �� 5^      -	   5         ��Name$ textBox��5^      $��8,8,18,0q�6     Note-Sketch#���  60   $	��Top=�6V   $ 0Q�6n   $+��%Quick shopping cart overview popup...��6z    �� TextWrapping$
 Wrap=�6�   $
��Auto��6�   $	��113��6�   5_      � 5`      -
   5        $ 
Chat_Popup��5`   	   $��
34,0,91,24q�6   $ 0Q�61   ���	#FF000000$��Bottom=�6T   $	��200��6o    5a        5b      $ 	MouseDown��6   . 5c      	  $ SnowboardScreens.Bindings��6   $ NoPopup��6G   5d      .+�5f        -   5        $ sketchRectangleUC1��5f      #���   66   ��6a   Z� �� 5g      -   5        $ textBox1��5g      $
��Left=�6   $��8,8,0,94q�6:   #���  6L   $ 0Q�6r   $2��,Chat with a friendly sales representative...��6~   $
 Wrap=�6�   $	��204��6�   $
��Auto��6�   5h      �� 5j      $
��Left=�6   $��8,8,0,0q�6)   $	��Top=�6:   $	��259��6R   $��26��6^   $��snowboard online��6j   $
��Wrap=�6�   $7��1/Snowboard.Screens;component/Fonts/#Buxton Sketch)�6�   $��24(�6�   �� 5k      $��Right=�6   $��0,8,8,0q�6*   $	��Top=�6;   $	��259��6S   $��26��6_   $7��1/Snowboard.Screens;component/Fonts/#Buxton Sketch)�6k   $��24(�6�   $��bindings��6�   $
��Wrap=�6�    �� TextAlignment$ Right=�6�   5l      5m      