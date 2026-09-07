FROM python:3.12-slim

WORKDIR /app

COPY exporter/jenkins_exporter.py .

RUN pip install --no-cache-dir flask requests

EXPOSE 8000

CMD ["python", "jenkins_exporter.py"]