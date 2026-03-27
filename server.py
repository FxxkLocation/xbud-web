from flask import Flask, request, jsonify, render_template
import requests
import json

app = Flask(__name__, template_folder='templates')

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/login', methods=['POST'])
def mock_login():
    data = request.json
    login_type = data.get('login_type')
    
    if login_type in ['phone', 'student'] and data.get('account'):
        return jsonify({"success": True, "message": "签名服务器连接正常，模拟登录通过", "token": "mock_token_123"})
    return jsonify({"success": False, "message": "账户或密码错误"})

@app.route('/api/map_data', methods=['GET'])
def mock_map():
    return jsonify({
        "restricted_area": [
            [43.838, 87.592],
            [43.838, 87.606],
            [43.826, 87.606],
            [43.826, 87.592]
        ],
        "points": [
            [43.833, 87.596],
            [43.830, 87.600]
        ],
        "current_location": [43.836, 87.598]
    })

@app.route('/api/run', methods=['POST'])
def mock_run():
    data = request.json
    
    # 向 Unidbg SO 服务请求签名
    try:
        sign_resp = requests.post("http://127.0.0.1:5001/sign/body", json={"distance": data.get('distance')}, timeout=3)
        sign_data = sign_resp.json()
        secret_key = sign_data.get('key', '未知Key')
    except Exception as e:
        return jsonify({"status": "error", "message": f"连接签名服务异常: {str(e)}"})

    return jsonify({
        "status": "success",
        "message": f"成功调用底层 SO 算法！提取到 Secret Key 为: {secret_key}",
        "mock_api_calls": [
            "POST https://campusapi.xbud.run/v4/user/login/login",
            "POST https://campusapi.xbud.run/v1/sport/run/get_config",
            "POST https://campusapi.xbud.run/v1/sport/run/get_fixed_point/...",
            "POST https://campusapi.xbud.run/v4/sport/run/save_record"
        ]
    })

if __name__ == '__main__':
    print("Starting Flask. Connected with Unidbg Java Core.")
    app.run(host='0.0.0.0', port=5000, debug=True)
