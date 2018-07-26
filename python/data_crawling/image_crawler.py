#-*-coding:utf-8
from selenium                       import webdriver
from selenium.webdriver.common.by   import By
from selenium.webdriver.support.ui  import WebDriverWait
from selenium.webdriver.support     import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from bs4                            import BeautifulSoup
from PIL                            import Image
import time
import random
import urllib.request
import os

def clear():
    if os.name == 'posix':
        os.system('clear')

    elif os.name in ('ce', 'nt', 'dos'):
        os.system('cls')

def displayText(text, mode="normal") :
    if (mode == "commit") :
        print('\x1b[1;33;40m' + text + '\x1b[0m')
    elif (mode == "success") :
        print('\x1b[1;32;40m' + text + '\x1b[0m')
    elif (mode == "fail") :
        print('\x1b[1;31;40m' + text + '\x1b[0m')
    else :
        print(text)


def execute(browser,scroll=False) :

    waitTime = 60
    # clear()
    # displayText("\n\n :: Welcome to Nike BOT :: \n\n","commit")

    # Load page ───────────────────────────────────────────────────────────────
    html            = browser.page_source
    soup            = BeautifulSoup(html, 'html.parser')
    openFlag        = False

    # Nike login (서버 시간 체크할때 로그인 )───────────────────────────────────────────────────────────────
    # displayText("[Notice] " + "Nike Login 진행 중...")
    # before_loginBtn = "/html/body/header/div[1]/div/ul/li[1]/span/a[2]"
    # after_loginBtn  = '//*[@id="common-modal"]/div/div/div/div[2]/div/div[2]/div/button'
    # try:
    #     element = WebDriverWait(browser, waitTime).until(
    #         EC.presence_of_element_located((By.XPATH, before_loginBtn))
    #     )
    # finally:
    #     browser.find_element_by_xpath(before_loginBtn).click()
    # try:
    #     element = WebDriverWait(browser, waitTime).until(
    #         EC.presence_of_element_located((By.XPATH, after_loginBtn))
    #     )
    # finally:
    #     browser.find_element_by_name('j_username').send_keys('memoming@naver.com')
    #     browser.find_element_by_name('j_password').send_keys('memo1504@!')
    #     # browser.find_element_by_class_name("brz-icon-checkbox").click()
    #     browser.find_element_by_xpath(after_loginBtn).click()
    #     displayText("[Notice] " + "Nike Login 완료.","success")
    #     time.sleep(1)

    # Select the size ───────────────────────────────────────────────────────────────
    while( not openFlag ) :
        try :
            openFlag = True
            if (scroll) : # 스크롤 방식 사이즈선택
                try :
                    element = WebDriverWait(browser, waitTime).until(
                        EC.presence_of_element_located((By.TAG_NAME, "option"))
                    )
                finally :
                    # TODO : 새로고침 했으나 아직 서버가 열리지 않은 경우를 위해
                    # TODO : 열리지 않았을때의 뭔가가 나왔다면 다시 새로고침 하도록
                    options         = soup.find_all('option')
                    abled_size      = list()
                    disabled_size   = list()
                    abled_size_dict = dict()

                    for index, item in enumerate(options) :
                        if ( index == 0 ) :
                            continue
                        else :
                            size = item["data-value"]
                            if ( "disabled" in item.attrs ) :
                                disabled_size.append( size )
                            else :
                                abled_size.append( size )
                                abled_size_dict[size] = index


                    selectedSize = random.choice(abled_size)
                    print("[Notice] 구매 가능 사이즈 ",abled_size)

                    select_all_size = browser.find_elements_by_tag_name("option")
                    sizeBtn         = select_all_size[abled_size_dict[selectedSize]]
                    nextBtn         = browser.find_element_by_xpath('//*[@id="btn-buy"]')

                    sizeBtn.click()
                    nextBtn.click()
                    displayText("[Notice] " + str(selectedSize) + " 사이즈 선택되었습니다.","success")

            else : # 선택 방식 사이즈선택
                try :
                    element = WebDriverWait(browser, waitTime).until(
                        EC.presence_of_element_located((By.XPATH, '//*[@id="btn-buy"]/span'))
                    )
                finally :
                    # TODO : 새로고침 했으나 아직 서버가 열리지 않은 경우를 위해
                    # TODO : 열리지 않았을때의 뭔가가 나왔다면 다시 새로고침 하도록
                    notices         = soup.find_all(id = "FW_SIZE1")
                    abled_size      = list()
                    disabled_size   = list()
                    abled_size_dict = dict()

                    for index, item in enumerate(notices) :
                        size = item["data-value"]
                        if ( "disabled" in item.attrs ) :
                            disabled_size.append( size )
                        else :
                            abled_size.append( size )
                            abled_size_dict[size] = index+1

                    selectedSize    = random.choice(abled_size)
                    sizeBlock       = "/html/body/section/section/section/article/article[2]/div/div[2]/div/div[2]/form/div[1]/div[2]/div[1]/div/span["+str(abled_size_dict[selectedSize])+"]"
                    browser.find_element_by_xpath(sizeBlock).click()
                    browser.find_element_by_xpath('//*[@id="btn-buy"]/span').click()
                    displayText("[Notice] " + str(selectedSize) + " 사이즈 선택되었습니다.","success")
        except :
            openFlag = False
            displayText("[Notice] 에러(Error Code:01)발생 ! 새로 고침 합니다.","fail")
            browser.refresh()


    # go next ───────────────────────────────────────────────────────────────
    displayText("[Notice] " + "구매 정보 입력 중...")
    flagBtn = '//*[@id="shipping_info"]/div[1]/ul/li[3]/div/span/label/span'
    nextBtn = '//*[@id="btn-next"]'
    try:
        element = WebDriverWait(browser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, flagBtn))
        )
    finally:
        browser.find_element_by_xpath(nextBtn).click()
        displayText("[Notice] " + "구매 정보 입력 완료.","success")


    # purchase ... 1 ───────────────────────────────────────────────────────────────
    displayText("[Notice] " + "결제 방식 선택 중...")
    currentHandler  = browser.get_window_position()
    agreeBtn        = '//*[@id="payment-review"]/div[1]/ul/li[2]/form/div/span/label/i'
    nextBtn         = '//*[@id="complete_checkout"]'
    try:
        element = WebDriverWait(browser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, agreeBtn))
        )
    finally:
        browser.find_element_by_xpath(agreeBtn).click()
        browser.find_element_by_xpath(nextBtn).click()
        displayText("[Notice] " + "결제 방식 선택 완료.","success")


    # purchase ... 2 (INICIS) ───────────────────────────────────────────────────────────────
    displayText("[Notice] " + "이니시스 결제 시스템 접속 중 ...")
    agreeBtn        = '//*[@id="inputAll"]'
    nextBtn         = '//*[@id="CardBtn"]'
    shinhanCardBtn  = '//*[@id="testLoad"]/div[2]/div[2]/div[3]/ul[2]/li[3]'
    samsungCardBtn  = '//*[@id="testLoad"]/div[2]/div[2]/div[3]/ul[1]/li[2]'
    paycoBtn        = '//*[@id="testLoad"]/div[2]/div[2]/div[2]/div/div/ul/li[1]'

    try:
        element = WebDriverWait(browser, waitTime).until(
            # browser.switch_to.window(window_name=browser.window_handles[0])
            # EC.presence_of_element_located((By.XPATH, "/html/body/div[10]/iframe"))
            EC.presence_of_element_located((By.XPATH, "/html/head/script[5]"))
            # EC.number_of_windows_to_be(num_windows=3) # TODO 일부러 에러
        )
    finally:
        browser.switch_to.window(window_name=browser.window_handles[0])

        # print(browser.find_elements_by_tag_name("iframe"))

        # with open("test.txt","w",encoding="utf-8") as txt :
        #     txt.write(browser.page_source)
        time.sleep(1)     #TODO
        browser.switch_to_frame(browser.find_element_by_xpath("/html/body/div[10]/iframe"));
        browser.switch_to_frame(browser.find_element_by_xpath('//*[@id="iframe"]'));
        displayText("[Notice] INICIS 결제 시스템 접속 완료.","success")
        try:
            displayText("[Notice] 결제 수단 선택 중...")
            element = WebDriverWait(browser, waitTime).until(
                EC.presence_of_element_located((By.XPATH, agreeBtn))
            )
        finally:
            time.sleep(1)
            browser.find_element_by_xpath(agreeBtn).click()
            browser.find_element_by_xpath(paycoBtn).click()
            browser.find_element_by_xpath(nextBtn).click()
            displayText("[Notice] Payco 결제 선택 완료.","success")


    # try:
    #     #TODO : 팝업창이 안뜰 수 있으므로 트리거 생각할것 (CLI는 안뜸)
    #     element = WebDriverWait(browser, waitTime).until(
    #         # browser.switch_to.window(window_name=browser.window_handles[0])
    #         EC.presence_of_element_located((By.XPATH, "/html/body/div[10]/iframe"))
    #         # EC.number_of_windows_to_be(num_windows=3) # TODO 일부러 에러
    #     )
    # finally:
    #     # TODO : 여기도 팝업창이 안뜰수 있으므로 다른 트리거 생각할것
    #     if (len(browser.window_handles) == 2):
    #         browser.switch_to.window(window_name=browser.window_handles[0])
    #         browser.switch_to_frame(browser.find_element_by_xpath("/html/body/div[10]/iframe"));
    #         browser.switch_to_frame(browser.find_element_by_xpath('//*[@id="iframe"]'));
    #         try:
    #             element = WebDriverWait(browser, waitTime).until(
    #                 EC.presence_of_element_located((By.XPATH, agreeBtn))
    #             )
    #         finally:
    #             time.sleep(1)
    #             browser.find_element_by_xpath(agreeBtn).click()
    #             browser.find_element_by_xpath(paycoBtn).click()
    #             browser.find_element_by_xpath(nextBtn).click()
    #             return

    # Payco Login ───────────────────────────────────────────────────────────────
    displayText("[Notice] Payco Login 진행 중...")
    paycoLoginbtn = '//*[@id="loginBtn"]'
    try:
        #TODO : window 창 개수가 아닌 다른 trigger를 생각해야함
        element = WebDriverWait(browser, waitTime).until(
            EC.number_of_windows_to_be(num_windows=3)
        )
    finally:
        browser.switch_to.window(window_name=browser.window_handles[-1])
        browser.find_element_by_xpath('//*[@id="id"]').send_keys("rldnr335@hanmail.net")
        browser.find_element_by_xpath('//*[@id="pw"]').send_keys("ghwn0524@")
        browser.find_element_by_xpath(paycoLoginbtn).click()
        displayText("[Notice] Payco Login 성공.","success")

    # Payco Payment ───────────────────────────────────────────────────────────────
    displayText("[Notice] Payco 결제 진행 중...")
    agreeBtn    = '//*[@id="form_agree5"]/span'
    paymentBtn  = '//*[@id="btnPayment"]'
    try:
        element = WebDriverWait(browser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, agreeBtn))
        )
    finally:
        browser.find_element_by_xpath(agreeBtn).click()
        browser.find_element_by_xpath(paymentBtn).click()
        displayText("[Notice] Payco 결제 카드 선택 완료.","success")


    # Payco Payment input pw ───────────────────────────────────────────────────────────────
    displayText("[Notice] Payco 결제 비밀번호 입력 중...")
    input_iframe = '//*[@id="body"]/div[5]/div/div/div/iframe'
    try:
        element  = WebDriverWait(browser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, input_iframe))
        )
    finally:
        browser.switch_to_frame(browser.find_element_by_xpath('//*[@id="body"]/div[5]/div/div/div/iframe'))
        html            = browser.page_source
        soup            = BeautifulSoup(html, 'html.parser')
        notices         = soup.find_all("a",class_ = "key")
        k               = 0
        payco_NumDict   = dict()
        for i in notices :
            imgId           = i["id"][2:]
            paycoImgPath    = "./temp/tempnum_"+str(k)+".png"
            imgurl          = "https://static2-bill.gslb.toastoven.net/payco/bill/v3/img/vk/"+imgId+".png"
            urllib.request.urlretrieve(imgurl, paycoImgPath)
            for j in range(10) :
                numPath     = "./data/payco_numbers/"+str(j)+".png"
                paycoImg    = Image.open(paycoImgPath)
                DBImage     = Image.open(numPath)
                if ( DBImage == paycoImg ) :
                    payco_NumDict[j] = imgId
            k += 1
        myPaycoPW           = "977412"
        for num in myPaycoPW :
            time.sleep(0.5)
            browser.find_element_by_id("A_"+payco_NumDict[int(num)]).click()
        displayText("[Notice] Payco 결제 비밀번호 입력완료.","success")

    # payment complete ───────────────────────────────────────────────────────────────
    displayText("[Notice] 최종 결제 마무리 중...")
    completeBtn = '//*[@id="payDoneBtn"]'
    try:
        browser.switch_to_active_element()
        element = WebDriverWait(browser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, completeBtn))
        )
    finally:
        browser.switch_to.window(window_name=browser.window_handles[0])
        browser.switch_to_frame(browser.find_element_by_xpath('/html/body/div[10]/iframe'));
        browser.switch_to_frame(browser.find_element_by_xpath('//*[@id="iframe"]'));
        browser.find_element_by_xpath(completeBtn).click()
        time.sleep(3)
        displayText("[Notice] 축하합니다 ! 해당 제품의 구매가 완료되었습니다.","success")
        browser.quit()
        exit()

def checkTime(targetURL, targetTime) : # targeTime = "00시00분00초"
    serverTimeURL   = "https://time.navyism.com/?host=" + targetURL
    serverTime      = '//*[@id="time_area"]'
    waitTime        = 60
    clear()
    displayText("Connecting ...","commit")
    timeBrowser     = getBrowser()
    targetBrowser   = getBrowser()
    clear()
    displayText("Browser Connected !","success")
    timeBrowser.get(serverTimeURL)
    targetBrowser.get(targetURL)

    clear()
    displayText("\n\n :: Welcome to Nike BOT :: \n\n","commit")

    # Nike login ───────────────────────────────────────────────────────────────
    displayText("[Notice] " + "Nike Login 진행 중...")
    before_loginBtn = '/html/body/header/div[1]/div/ul/li[1]/span/a[2]'
    after_loginBtn  = '//*[@id="common-modal"]/div/div/div/div[2]/div/div[2]/div/button'
    try:
        element = WebDriverWait(targetBrowser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, before_loginBtn))
        )
    finally:
        targetBrowser.find_element_by_xpath(before_loginBtn).click()
    try:
        element = WebDriverWait(targetBrowser, waitTime).until(
            EC.presence_of_element_located((By.XPATH, after_loginBtn))
        )
    finally:
        targetBrowser.find_element_by_name('j_username').send_keys('memoming@naver.com')
        targetBrowser.find_element_by_name('j_password').send_keys('memo1504@!')
        # targetBrowser.find_element_by_class_name("brz-icon-checkbox").click() # 로그인 정보해제
        targetBrowser.find_element_by_xpath(after_loginBtn).click()
        displayText("[Notice] " + "Nike Login 완료.","success")

    # Servertime Check  ───────────────────────────────────────────────────────────────
    displayText("[Notice] 서버시간 체크 중 ... [설정시간 : " + targetTime + "]","success")
    flag = True
    while(flag) :
        html            = timeBrowser.page_source
        soup            = BeautifulSoup(html, 'html.parser')
        serverTime      = soup.find("div",id = "time_area")
        serverTimeList  = serverTime.text.split(' ') # 0:년 / 1:월 / 2:일 / 3:시 / 4:분 / 5초
        if (targetTime == serverTimeList[3]+serverTimeList[4]+serverTimeList[5]) :
            targetBrowser.refresh()
            displayText("\n[Notice] It's time to FIGHT !!!! ","success")
            flag = False
            if ( url.endswith("c=snkrs") ) :
                execute(targetBrowser,True)
            else :
                execute(targetBrowser)
            timeBrowser.quit()
        else :
            print('\r\x1b[1;31;40m[Notice] 아직 오픈 전 입니다.\x1b[0m' + '( '+ serverTime.text +' )', end='')



def getBrowser() :
    options         = webdriver.ChromeOptions()
    # options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument("--disable-setuid-sandbox")
    options.add_argument("--ignore-certificate-errors")
    options.add_argument('window-size=1920x1080')
    #TODO : user-agent 확인하기
    # options.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
    # options.add_argument("--disable-extensions")
    # options.add_argument("--incognito")
    # options.add_argument('--disable-dev-shm-usage')
    browser         = webdriver.Chrome(executable_path="tools/webdriver/chrome_win/chromedriver",chrome_options=options) # win
    # browser         = webdriver.Chrome(executable_path="tools/webdriver/chrome_linux/chromedriver",chrome_options=options) # linux
    browser.wait    = WebDriverWait(browser, 1)
    return browser



if ( __name__ == "__main__") :



    url = "https://www.nike.com/kr/ko_kr/t/women/fw/nike-sportswear/917691-100/xcqz65/wmns-air-max-1-lx?c=snkrs"
    # browser.get(url)


    checkTime(targetURL=url, targetTime="10시00분00초")
    # test(browser_1,browser_2)
