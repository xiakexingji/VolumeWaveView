## VolumeWaveView （音量条形与波浪）

### 截图
![image](https://github.com/conghuahuadan/VolumeWaveView/blob/master/screenshots/GIF.gif?raw=true)

### 使用
```xml
<com.chhd.volumewaveview.VolumeWaveView
    android:id="@+id/wave_view"
    android:layout_width="match_parent"
    android:layout_height="100dp"
    android:background="#eee" />
```

### 属性
```
vwv_light_color                     : 高亮条形颜色
vwv_un_light_color                  : 非高亮条形颜色
vwv_column_width                    : 亮条宽度
vwv_column_offset                   : 亮条间隔
vwv_is_pause                        : 是否默认暂停
```

### 备注
高亮进度根据当前媒体音量变化
