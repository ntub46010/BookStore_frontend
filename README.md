# BookStore_frontend
大學專題製作的Android APP，完整版第一版，後端為Node.js。<br>
之後打算將連線方式從AsyncTask改為OKHttp，並整理雜亂的程式碼。<br>
前陣子在開發將後端改為Tomcat後，以此為基礎的前端APP。連結：https://github.com/ntub46010/ShareMe_Tomcat

## 登入畫面
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/LoginActivity.java">LoginActivity</a><br>
說明：輸入帳密才可登入，或者前往註冊帳號。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%99%BB%E5%85%A5%E7%95%AB%E9%9D%A2.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E8%A8%BB%E5%86%8A(%E7%A9%BA%E7%99%BD).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E8%A8%BB%E5%86%8A(%E6%9C%89%E8%B3%87%E6%96%99).png" height="24%" width="24%" />
</td></tr></table>

## 首頁
主程式：
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/MainActivity.java">MainActivity</a>、
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/type/DepartmentFrag.java">DepartmentFrag</a>、
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/product/ProductHomeFrag.java">ProductHomeFrag</a>、
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberHomeFrag.java">MemberHomeFrag</a>、
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/setting/SettingHomeFrag.java">SettingHomeFrag</a>
<br>
說明：由4個Fragment呈現主要功能，可左右滑動切換。點選各個科系能查看該分類的商品。商品首頁上方的放大鏡圖案是搜尋功能。設定的通知開關是控制推播通知的顯示。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%A7%91%E7%B3%BB.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E9%A6%96%E9%A0%81.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%9C%83%E5%93%A1%E5%B0%88%E5%8D%80.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E8%A8%AD%E5%AE%9A.png" height="24%" width="24%" />
</td></tr></table>

## 搜尋商品
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/product/ProductSearchActivity.java">ProductSearchActivity</a><br>
說明：輸入關鍵字，以找出當前所在分類下，書名包含該字眼的商品。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%9C%83%E8%B3%87%E7%B3%BB%E5%95%86%E5%93%81.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%90%9C%E5%B0%8B(%E9%A6%96%E9%A0%81).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%90%9C%E5%B0%8B(%E7%B5%90%E6%9E%9C).png" height="24%" width="24%" />
</td></tr></table>

## 刊登商品
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/product/ProductPostActivity.java">ProductPostActivity</a><br>
說明：從手機圖庫中選取照片，依固定長寬比例裁切，最多選5張。並填寫書名、價格、筆記等資料，以及所要刊登在的分類(可複選)。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%88%8A%E7%99%BB%E5%95%86%E5%93%81(%E7%A9%BA%E7%99%BD).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%88%8A%E7%99%BB%E5%95%86%E5%93%81(%E8%A3%81%E5%9C%96).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%88%8A%E7%99%BB%E5%95%86%E5%93%81(%E6%9C%89%E8%B3%87%E6%96%99).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%88%8A%E7%99%BB%E5%95%86%E5%93%81(%E4%B8%8A%E5%82%B3%E4%B8%AD).png" height="24%" width="24%" />
</td></tr></table>

## 商品詳情
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/product/ProductDetailActivity.java">ProductDetailActivity</a><br>
說明：顯示商品詳細資料，可放大圖片。點擊愛心圖案會加入最愛，供日後查看。點擊信封圖案會開啟交談室，以聯絡賣家。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E8%A9%B3%E6%83%851.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E8%A9%B3%E6%83%852.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E8%A9%B3%E6%83%85(%E5%9C%96%E7%89%87%E7%B8%AE%E6%94%BE).png" height="24%" width="24%" />
</td></tr></table>

## 個人檔案
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberProfileActivity.java">MemberProfileActivity</a><br>
說明：顯示個人基本資料，如大頭照、姓名、科系與上架商品。點擊信封圖案可開啟Email軟體，寄信給對方。點擊評價圖案可給予對方正評或負評，或者撤回。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E8%87%AA%E5%B7%B1).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E8%B3%A3%E5%AE%B6).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E8%B2%B7%E5%AE%B6).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(Email).png" height="24%" width="24%" />
</td></tr></table>

## 我的最愛
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberFavoriteActivity.java">MemberFavoriteActivity</a><br>
說明：先前在商品詳情中按過愛心圖案的商品，可在此查看。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%88%91%E7%9A%84%E6%9C%80%E6%84%9B.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E6%88%91%E7%9A%84%E6%9C%80%E6%84%9B(%E6%9C%AA%E6%89%BE%E5%88%B0).png" height="24%" width="24%" />
</td></tr></table>

## 交談室
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberChatActivity.java">MemberChatActivity</a><br>
說明：買賣雙方對話的地方，在此洽談交易事宜。點擊右上方手提袋圖案將出現半透明背景的商品詳情。點擊人形圖案開啟對方個人檔案。點擊中上方選單則可切換到其他談過的商品。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BA%A4%E8%AB%87%E5%AE%A41.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BA%A4%E8%AB%87%E5%AE%A4(%E7%9C%8B%E5%95%86%E5%93%81).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BA%A4%E8%AB%87%E5%AE%A42.png" height="24%" width="24%" />
</td></tr></table>

## 信箱
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberMailboxActivity.java">MemberMailboxActivity</a><br>
說明：顯示與各個會員的對話紀錄，點擊可開啟交談室。另外接收到他人的訊息時將會收到推播通知，在交談室以外的地方，包含關閉本App後都可收到推播。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BF%A1%E7%AE%B1.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BF%A1%E7%AE%B1(%E6%9C%AA%E6%89%BE%E5%88%B0).png" height="24%" width="24%" />
</td></tr></table>

## 商品管理
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/MemberStockActivity.java">MemberStockActivity</a>、
<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/member/ProductEditActivity.java">ProductEditActivity</a>
<br>
說明：對自己刊登的商品進行查看、編輯與下架的操作。其中編輯功能可追加、移動或移除圖片。下架功能使該商品不會再被搜尋到，但仍可在交談室中被檢視。

### 選項
<table>
<tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E7%AE%A1%E7%90%86(%E9%A6%96%E9%A0%81).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E7%AE%A1%E7%90%86(%E9%81%B8%E9%A0%85).png" height="24%" width="24%" />
</td></tr></table>

### 編輯
<table>
<tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%95%86%E5%93%81(%E8%B5%B7%E5%88%9D).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%95%86%E5%93%81.png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%95%86%E5%93%81(%E5%8B%95%E5%9C%96%E7%89%87).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E8%A9%B3%E6%83%85(%E7%B7%A8%E8%BC%AF%E5%BE%8C).png" height="24%" width="24%" />
</td></tr>
</table>

### 下架
<table>
<tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E5%95%86%E5%93%81%E7%AE%A1%E7%90%86(%E4%B8%8B%E6%9E%B6).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E4%BA%A4%E8%AB%87%E5%AE%A4(%E5%B7%B2%E4%B8%8B%E6%9E%B6).png" height="24%" width="24%" />
</td></tr>
</table>

## 帳號設定
主程式：<a href="https://github.com/ntub46010/BookStore_frontend/blob/master/app/src/main/java/com/xy/psn/setting/SettingProfileActivity.java">SettingProfileActivity</a><br>
說明：編輯個人基本資料，包含姓名、科系、Email、大頭照與登入密碼。
<table><tr><td>
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E8%B5%B7%E5%88%9D).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E5%A4%A7%E9%A0%AD%E7%85%A7).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E5%AF%86%E7%A2%BC%E9%8C%AF%E8%AA%A4).png" height="24%" width="24%" />
<img src="https://github.com/ntub46010/Photos/blob/master/BookStore%E6%93%8D%E4%BD%9C%E7%95%AB%E9%9D%A2/%E7%B7%A8%E8%BC%AF%E5%80%8B%E4%BA%BA%E6%AA%94%E6%A1%88(%E4%B8%8A%E5%82%B3).png" height="24%" width="24%" />
</td></tr></table>
