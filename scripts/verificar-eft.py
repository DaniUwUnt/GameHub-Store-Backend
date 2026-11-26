"""Prueba de aceptación. Ejecutar únicamente contra la demo local con datos sintéticos."""
import json
import os
import time
import uuid
from pathlib import Path
from urllib.request import Request, build_opener, ProxyHandler
from urllib.error import HTTPError, URLError
URL = 'http://127.0.0.1:8080'
http = build_opener(ProxyHandler({}))
def llamada(metodo,ruta,esperado=200,cuerpo=None,token=None):
    headers={'Accept':'application/json'}
    if cuerpo is not None: headers['Content-Type']='application/json'
    if token: headers['Authorization']='Bearer '+token
    try:
        with http.open(Request(URL+ruta,data=None if cuerpo is None else json.dumps(cuerpo).encode(),headers=headers,method=metodo),timeout=20) as r:
            codigo=r.status; datos=r.read()
    except HTTPError as e: codigo=e.code; datos=e.read()
    if codigo!=esperado: raise RuntimeError(f'{metodo} {ruta}: esperado {esperado}; recibido {codigo}')
    print(f'{metodo} {ruta.split("?")[0]} -> {codigo}',flush=True)
    return json.loads(datos) if datos else None
def main():
    clave=os.environ.get('CLAVE_DEMO')
    if not clave:
        env=(Path(__file__).resolve().parent.parent/'.env').read_text()
        clave=next(l.split('=',1)[1].strip() for l in env.splitlines() if l.startswith('CLAVE_DEMO='))
    for intento in range(120):
        try:
            catalogo=llamada('GET','/api/v1/productos')
            if catalogo: break
        except (URLError,RuntimeError,TimeoutError): pass
        time.sleep(3)
    else: raise RuntimeError('Gateway o catálogo no quedaron listos; revisa .runtime.')
    def login(nombre):
        return llamada('POST','/api/v1/auth/login',cuerpo={'email':nombre+'.levelup@duoc.cl','clave':clave})['token']
    admin=login('ana'); cliente=login('camila'); vendedor=login('luis')
    llamada('POST','/api/v1/auth/renovar',token=cliente)
    llamada('GET','/api/v1/admin/usuarios',403,token=cliente)
    llamada('GET','/api/v1/admin/usuarios',403,token=vendedor)
    cat='/api/v1/admin/categorias'; etiqueta='EFT '+uuid.uuid4().hex[:7]
    idc=llamada('POST',cat,201,{'nombre':etiqueta},admin)['id']
    assert any(x['id']==idc for x in llamada('GET',cat,token=admin))
    editada=llamada('PUT',cat+'/'+str(idc),200,{'nombre':etiqueta+' final'},admin)
    assert editada['nombre'].endswith(' final')
    llamada('DELETE',cat+'/'+str(idc),204,token=admin)
    assert not any(x['id']==idc for x in llamada('GET',cat,token=admin))
    llamada('POST','/api/v1/contacto',400,{'nombre':'Demo','comentario':''})
    ticket=llamada('POST','/api/v1/contacto',201,{'nombre':'Demo EFT','comentario':'Consulta de prueba'})['id']
    llamada('GET','/api/v1/admin/contacto',403,token=cliente)
    assert any(x['id']==ticket for x in llamada('GET','/api/v1/admin/contacto',token=admin))
    stock_url='/api/v1/inventario/stock?productos='+','.join(str(p['id']) for p in catalogo)
    stock=llamada('GET',stock_url)
    pid=next(x['productoId'] for x in stock if x['stock']>0 and any(p['id']==x['productoId'] for p in catalogo))
    antes=next(x['stock'] for x in stock if x['productoId']==pid)
    cuerpo={'lineas':[{'productoId':pid,'cantidad':1}],'metodoPago':'DEBITO','despacho':{'nombre':'Cliente Demo','correo':'camila.levelup@duoc.cl','telefono':'912345678','region':'Región Metropolitana de Santiago','comuna':'Ñuñoa','direccion':'Calle de Prueba 123'}}
    orden=llamada('POST','/api/v1/ordenes',201,cuerpo,cliente)
    assert any(o['id']==orden['id'] for o in llamada('GET','/api/v1/ordenes/mias',token=cliente))
    assert next(x['stock'] for x in llamada('GET',stock_url) if x['productoId']==pid)==antes-1
    print('OK: roles, renovación, CRUD, contacto y compra con stock persistido. Sólo datos de prueba.')
if __name__=='__main__': main()
