# Use an official Python runtime as a parent image
FROM python:3.11-slim

# Update system tar to pick up security fixes
RUN apt-get update \
	&& apt-get install -y --no-install-recommends tar \
	&& rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /code

# Upgrade pip to a patched version
RUN pip install --no-cache-dir --upgrade pip==25.3

# Copy the dependencies file to the working directory
COPY ./requirements.txt /code/requirements.txt

# Install packages with normal dependency resolution
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

# Force Starlette to the patched version, overriding FastAPI's upper bound
RUN pip install --no-cache-dir --no-deps starlette==0.49.1

# Copy the rest of the application code
COPY ./app /code/app

# Make port 80 available to the world outside this container
EXPOSE 80

# Run the application with Granian
# We use a shell command to dynamically set the number of workers based on available CPU cores
CMD ["sh", "-c", "granian --interface asgi --host 0.0.0.0 --port ${PORT:-80} --workers ${WORKERS:-$(nproc)} app.main:app"]
